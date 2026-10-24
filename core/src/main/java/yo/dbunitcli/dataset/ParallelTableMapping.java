package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.ThreadContexts;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * ソースを別スレッドで並列に読み込む。
 * 出力テーブルを共有するソースは同じレーンにまとめ、レーン内は元の順に逐次処理するため、
 * 同じテーブルへの追記順は逐次実行と変わらない。作業スレッド（ワーカー）ごとにコンテキスト・converter・DB接続を持ち、
 * ワーカーはレーンを順に取り出して処理するので、ワーカー間で状態を共有しない。
 */
public final class ParallelTableMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelTableMapping.class);

    private static final AtomicInteger THREAD_NO = new AtomicInteger();

    private ParallelTableMapping() {
    }

    /**
     * タスクを列挙せずに判定できる条件で、並列化の余地があるか
     */
    public static boolean isParallelizable(final ComparableDataSetProducer producer) {
        final ComparableDataSetParam param = producer.param();
        return ParallelTableMapping.parallelism(param) > 1
                && producer.isParallelizableSource()
                && (param.converter() == null || param.converter().isParallelizable())
                && ParallelTableMapping.isIndependentTables(param.tableSeparators());
    }

    /**
     * @return 並列化できる場合はレーンの一覧（2つ以上）。できない場合はempty
     */
    public static Optional<List<List<ComparableTableMappingTask>>> lanes(final ComparableDataSetProducer producer
            , final List<ComparableTableMappingTask> tasks) {
        if (tasks.size() < 2 || !ParallelTableMapping.isParallelizable(producer)) {
            return Optional.empty();
        }
        final List<List<ComparableTableMappingTask>> result = ParallelTableMapping.groupBySharedTable(producer.param().tableSeparators(), tasks);
        if (result.size() < 2) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public static TreeMap<String, ComparableTable> run(final ComparableDataSetParam param
            , final List<List<ComparableTableMappingTask>> lanes) {
        final int threads = Math.min(ParallelTableMapping.parallelism(param), lanes.size());
        ParallelTableMapping.LOGGER.info("produce() - parallel lanes={}, threads={}", lanes.size(), threads);
        final Queue<List<ComparableTableMappingTask>> queue = new ConcurrentLinkedQueue<>(lanes);
        final ExecutorService executor = Executors.newFixedThreadPool(threads, runnable -> {
            final Thread thread = new Thread(runnable, "produce-" + ParallelTableMapping.THREAD_NO.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        // executeは投入元のスレッドで呼ばれるため、ここで投入元の文脈を包む
        final CompletionService<TreeMap<String, ComparableTable>> completion = new ExecutorCompletionService<>(
                command -> executor.execute(ThreadContexts.propagate(command)));
        final List<Future<TreeMap<String, ComparableTable>>> futures = IntStream.range(0, threads)
                .mapToObj(i -> completion.submit(() -> ParallelTableMapping.runWorker(param, queue, i == 0)))
                .toList();
        final TreeMap<String, ComparableTable> result = new TreeMap<>();
        try {
            for (int i = 0; i < threads; i++) {
                result.putAll(completion.take().get());
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
     * 出力テーブル名を1つでも共有するソースを同じレーンにまとめる（連結成分）。レーン内・レーン間とも元の順を保つ。
     * 出力テーブルの候補はxls/xlsxでファイルを開くため、タスクごとに並列に求める
     */
    private static List<List<ComparableTableMappingTask>> groupBySharedTable(final TableSeparators tableSeparators
            , final List<ComparableTableMappingTask> tasks) {
        final List<List<String>> outputTables = tasks.parallelStream()
                .map(task -> task.outputTableCandidates()
                        .flatMap(tableSeparators::getAddSettingTableMetaData)
                        .map(AddSettingTableMetaData::getTableName)
                        .toList())
                .toList();
        final int[] parent = new int[tasks.size()];
        final Map<String, Integer> firstWriter = new HashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            parent[i] = i;
            for (final String tableName : outputTables.get(i)) {
                final Integer other = firstWriter.putIfAbsent(tableName, i);
                if (other != null) {
                    ParallelTableMapping.union(parent, other, i);
                }
            }
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
     * キューが空になるまでレーンを取り出して処理する。converter・DB接続はワーカー単位で確保してレーン間で使い回す。
     * レーンごとに出力テーブルが異なるので、コンテキストもワーカーで1つにできる
     *
     * @param first 最初のワーカーは元のconverterとタスクをそのまま使う（DB接続を余分に張らない）
     */
    private static TreeMap<String, ComparableTable> runWorker(final ComparableDataSetParam param
            , final Queue<List<ComparableTableMappingTask>> queue, final boolean first) {
        try (final WorkerResources resources = new WorkerResources()) {
            final IDataSetConverter converter = first || param.converter() == null
                    ? param.converter()
                    : param.converter().splitForParallel(resources);
            final ComparableTableMappingContext context = new ComparableTableMappingContext(param.tableSeparators(), converter);
            context.open();
            List<ComparableTableMappingTask> lane;
            while ((lane = queue.poll()) != null) {
                for (final ComparableTableMappingTask task : lane) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new CancellationException("produce interrupted");
                    }
                    (first ? task : task.forWorker(resources)).run(context);
                }
            }
            return context.close();
        }
    }

    /**
     * 作業スレッドごとに確保する資源（DB接続など）。同じkeyはスレッド内で使い回し、スレッドの処理終了時（失敗時も）に閉じる
     */
    public static final class WorkerResources implements AutoCloseable {

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
                        ParallelTableMapping.LOGGER.warn("failed to close worker resource:{}", k, e);
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
