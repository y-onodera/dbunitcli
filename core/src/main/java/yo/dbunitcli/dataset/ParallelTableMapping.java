package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.FileResources;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * ソースを別スレッドで並列に読み込む。
 * 出力テーブルを共有するソースは同じレーンにまとめ、レーン内は元の順に逐次処理するため、
 * 同じテーブルへの追記順は逐次実行と変わらない。レーンごとにコンテキストとconverterを分けるので、レーン間で状態を共有しない。
 */
public final class ParallelTableMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelTableMapping.class);

    private static final AtomicInteger THREAD_NO = new AtomicInteger();

    private static volatile UnaryOperator<Runnable> taskDecorator = UnaryOperator.identity();

    private ParallelTableMapping() {
    }

    /**
     * 作業スレッドへ渡すタスクを包む関数を設定する。
     * 関数は投入元のスレッドで呼ばれるので、投入元の文脈（ログの振り分け先など）を作業スレッドへ引き継ぐのに使う。
     */
    public static void setTaskDecorator(final UnaryOperator<Runnable> decorator) {
        ParallelTableMapping.taskDecorator = decorator == null ? UnaryOperator.identity() : decorator;
    }

    /**
     * @return 並列化できる場合はレーンの一覧（2つ以上）。できない場合はempty
     */
    public static Optional<List<List<ComparableTableMappingTask>>> lanes(final ComparableDataSetProducer producer
            , final List<ComparableTableMappingTask> tasks) {
        final ComparableDataSetParam param = producer.param();
        if (ParallelTableMapping.parallelism(param) <= 1
                || tasks.size() < 2
                || !producer.isParallelizableSource()
                || (param.converter() != null && !param.converter().isParallelizable())
                || !ParallelTableMapping.isIndependentTables(param.tableSeparators())) {
            return Optional.empty();
        }
        final List<List<ComparableTableMappingTask>> result = ParallelTableMapping.groupBySharedTable(param.tableSeparators(), tasks);
        if (result.size() < 2) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public static TreeMap<String, ComparableTable> run(final ComparableDataSetParam param
            , final List<List<ComparableTableMappingTask>> lanes) {
        final int threads = Math.min(ParallelTableMapping.parallelism(param), lanes.size());
        ParallelTableMapping.LOGGER.info("produce() - parallel lanes={}, threads={}", lanes.size(), threads);
        final BlockingQueue<Future<TreeMap<String, ComparableTable>>> completed = new LinkedBlockingQueue<>();
        final ExecutorService executor = Executors.newFixedThreadPool(threads, runnable -> {
            final Thread thread = new Thread(runnable, "produce-" + ParallelTableMapping.THREAD_NO.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        final List<FutureTask<TreeMap<String, ComparableTable>>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < lanes.size(); i++) {
                final List<ComparableTableMappingTask> lane = lanes.get(i);
                final boolean first = i == 0;
                final FutureTask<TreeMap<String, ComparableTable>> future = new FutureTask<>(() -> ParallelTableMapping.runLane(param, lane, first)) {
                    @Override
                    protected void done() {
                        completed.add(this);
                    }
                };
                futures.add(future);
                executor.execute(ParallelTableMapping.taskDecorator.apply(FileResources.propagateContext(future)));
            }
            for (int i = 0; i < futures.size(); i++) {
                completed.take().get();
            }
        } catch (final ExecutionException e) {
            futures.forEach(it -> it.cancel(true));
            throw ParallelTableMapping.rethrow(e.getCause());
        } catch (final InterruptedException e) {
            futures.forEach(it -> it.cancel(true));
            Thread.currentThread().interrupt();
            throw new CancellationException("produce interrupted");
        } finally {
            ParallelTableMapping.shutdown(executor);
        }
        final TreeMap<String, ComparableTable> result = new TreeMap<>();
        futures.forEach(it -> result.putAll(it.resultNow()));
        return result;
    }

    static int parallelism(final ComparableDataSetParam param) {
        if (param.parallel() == null || param.parallel() <= 0) {
            return Runtime.getRuntime().availableProcessors();
        }
        return param.parallel();
    }

    /**
     * joinはレーンをまたいで結果を共有し、件数分割は分割後の名前を事前に決められないため並列化しない
     */
    private static boolean isIndependentTables(final TableSeparators tableSeparators) {
        TableSeparators target = tableSeparators;
        while (target != null && target != TableSeparators.NONE) {
            if (!target.joinConditions().isEmpty() || target.hasSplitter()) {
                return false;
            }
            target = target.next();
        }
        return true;
    }

    /**
     * 出力テーブル名を1つでも共有するソースを同じレーンにまとめる（連結成分）。レーン内・レーン間とも元の順を保つ
     */
    private static List<List<ComparableTableMappingTask>> groupBySharedTable(final TableSeparators tableSeparators
            , final List<ComparableTableMappingTask> tasks) {
        final int[] parent = new int[tasks.size()];
        final Map<String, Integer> firstWriter = new HashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            parent[i] = i;
            final int index = i;
            tasks.get(i).outputTableCandidates()
                    .flatMap(tableSeparators::getAddSettingTableMetaData)
                    .map(AddSettingTableMetaData::getTableName)
                    .forEach(tableName -> {
                        final Integer other = firstWriter.putIfAbsent(tableName, index);
                        if (other != null) {
                            ParallelTableMapping.union(parent, other, index);
                        }
                    });
        }
        final Map<Integer, List<ComparableTableMappingTask>> result = new LinkedHashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            result.computeIfAbsent(ParallelTableMapping.find(parent, i), k -> new ArrayList<>()).add(tasks.get(i));
        }
        return new ArrayList<>(result.values());
    }

    private static int find(final int[] parent, final int i) {
        int root = i;
        while (parent[root] != root) {
            root = parent[root];
        }
        return root;
    }

    private static void union(final int[] parent, final int a, final int b) {
        final int rootA = ParallelTableMapping.find(parent, a);
        final int rootB = ParallelTableMapping.find(parent, b);
        // 根を小さい番号に揃え、レーンの並びを最初のソースの順にする
        parent[Math.max(rootA, rootB)] = Math.min(rootA, rootB);
    }

    /**
     * @param first 最初のレーンは元のconverterをそのまま使う（DB出力で接続を余分に張らない）
     */
    private static TreeMap<String, ComparableTable> runLane(final ComparableDataSetParam param
            , final List<ComparableTableMappingTask> lane, final boolean first) {
        final IDataSetConverter converter = ParallelTableMapping.laneConverter(param.converter(), first);
        try (final LaneResources resources = new LaneResources()) {
            final ComparableTableMappingContext context = new ComparableTableMappingContext(param.tableSeparators(), converter);
            context.open();
            for (final ComparableTableMappingTask task : lane) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new CancellationException("produce interrupted");
                }
                (first ? task : task.forLane(resources)).run(context);
            }
            return context.close();
        } finally {
            if (converter != null && converter != param.converter()) {
                converter.release();
            }
        }
    }

    /**
     * レーン（スレッド）ごとに確保する資源（DB接続など）。同じkeyはレーン内で使い回し、レーン終了時に閉じる
     */
    public static final class LaneResources implements AutoCloseable {

        private final Map<Object, Object> resources = new LinkedHashMap<>();

        private final List<Runnable> closers = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public <T> T get(final Object key, final Supplier<T> factory, final Closer<T> closer) {
            return (T) this.resources.computeIfAbsent(key, k -> {
                final T resource = factory.get();
                this.closers.add(() -> {
                    try {
                        closer.close(resource);
                    } catch (final Exception e) {
                        ParallelTableMapping.LOGGER.warn("failed to close lane resource:{}", k, e);
                    }
                });
                return resource;
            });
        }

        @Override
        public void close() {
            this.closers.forEach(Runnable::run);
        }

        @FunctionalInterface
        public interface Closer<T> {
            void close(T resource) throws Exception;
        }
    }

    private static IDataSetConverter laneConverter(final IDataSetConverter converter, final boolean first) {
        if (converter == null || first) {
            return converter;
        }
        return converter.splitForParallel();
    }

    private static RuntimeException rethrow(final Throwable cause) {
        if (cause instanceof final RuntimeException runtimeException) {
            return runtimeException;
        }
        if (cause instanceof final Error error) {
            throw error;
        }
        throw new AssertionError(cause);
    }

    /**
     * 作業スレッドが終わるまで待つ（逐次実行と同様に、戻った後で出力が書き換わらないようにする）
     */
    private static void shutdown(final ExecutorService executor) {
        executor.shutdownNow();
        boolean interrupted = false;
        while (true) {
            try {
                if (executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    break;
                }
            } catch (final InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
