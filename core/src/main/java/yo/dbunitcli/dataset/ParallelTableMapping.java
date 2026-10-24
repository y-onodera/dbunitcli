package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.ThreadContexts;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
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

    /**
     * forEachByKeyの作業スレッドごとの待ち行列の上限
     */
    private static final int QUEUE_CAPACITY = 64;

    private static final Object END_OF_ITEMS = new Object();

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
        final TreeMap<String, ComparableTable> result = new TreeMap<>();
        ParallelTableMapping.runWorkers(threads, "produce-", i -> () -> ParallelTableMapping.runWorker(param, queue, i == 0))
                .forEach(result::putAll);
        return result;
    }

    /**
     * itemsを作業スレッドで並列に処理する（処理順は不定）。ワーカーはキューが空になるまでitemを取り出して処理し、
     * DB接続などの資源はワーカー単位で確保して使い回す。いずれかが失敗したら残りを中断して例外を投げる
     *
     * @param action 最初のワーカー（first=true）は元の資源をそのまま使う（DB接続を余分に張らない）
     */
    public static <T> void forEach(final ComparableDataSetParam param, final List<T> items, final WorkerAction<T> action) {
        final int threads = Math.min(ParallelTableMapping.parallelism(param), items.size());
        ParallelTableMapping.LOGGER.info("forEach() - parallel items={}, threads={}", items.size(), threads);
        final Queue<T> queue = new ConcurrentLinkedQueue<>(items);
        ParallelTableMapping.runWorkers(threads, "generate-", i -> () -> {
            try (final WorkerResources resources = new WorkerResources()) {
                T item;
                while ((item = queue.poll()) != null) {
                    ParallelTableMapping.checkInterrupted();
                    action.accept(item, resources, i == 0);
                }
            }
            return null;
        });
    }

    /**
     * sourceが渡すitemを並列に処理する。keyが同じitemは同じ作業スレッドへ渡し、同じ投入元から渡した順に処理するため、
     * 同じ出力先へ書くitemは投入順になる。作業スレッドごとの待ち行列に上限を設け、投入元を待たせる（先読みしすぎない）。
     * sourceは受け取った関数を複数スレッドから呼んでよい。呼び出し元の文脈を作業スレッドへ引き継ぎ、
     * いずれかが失敗したら残りを中断して例外を投げる。戻る前に作業スレッドの終了を待つ
     */
    public static <T> void forEachByKey(final int threads, final Function<? super T, ?> key
            , final Consumer<? super T> action, final Consumer<Consumer<T>> source) {
        ParallelTableMapping.LOGGER.info("forEachByKey() - parallel threads={}", threads);
        final KeyedWorkers<T> workers = new KeyedWorkers<>(threads, key, action);
        try {
            source.accept(workers::submit);
            workers.finish();
        } catch (final RuntimeException | Error e) {
            workers.cancel();
            throw e;
        } finally {
            ParallelTableMapping.shutdown(workers.executor);
        }
    }

    private static final class KeyedWorkers<T> {

        private final List<BlockingQueue<Object>> queues;

        private final Function<? super T, ?> key;

        private final ExecutorService executor;

        private final List<Future<?>> futures;

        private KeyedWorkers(final int threads, final Function<? super T, ?> key, final Consumer<? super T> action) {
            this.key = key;
            this.queues = IntStream.range(0, threads)
                    .<BlockingQueue<Object>>mapToObj(i -> new ArrayBlockingQueue<>(ParallelTableMapping.QUEUE_CAPACITY))
                    .toList();
            this.executor = ParallelTableMapping.newExecutor(threads, "output-");
            // submitは作成元のスレッドで呼ばれるため、ここで作成元の文脈を包む
            this.futures = this.queues.stream()
                    .<Future<?>>map(queue -> this.executor.submit(ThreadContexts.propagate(() -> KeyedWorkers.drain(queue, action))))
                    .toList();
        }

        /**
         * itemをkeyに対応する作業スレッドの待ち行列へ入れる（空くまで待つ）。作業スレッドが失敗していれば、その例外を投げる
         */
        private void submit(final T item) {
            this.put(this.queues.get(Math.floorMod(Objects.hashCode(this.key.apply(item)), this.queues.size())), item);
        }

        /**
         * 投入の終わりを伝え、全ての作業スレッドの処理が終わるまで待つ。失敗していればその例外を投げる
         */
        private void finish() {
            this.queues.forEach(queue -> this.put(queue, ParallelTableMapping.END_OF_ITEMS));
            this.futures.forEach(ParallelTableMapping::await);
        }

        private void cancel() {
            this.futures.forEach(it -> it.cancel(true));
        }

        private void put(final BlockingQueue<Object> queue, final Object item) {
            try {
                while (!queue.offer(item, 100, TimeUnit.MILLISECONDS)) {
                    this.futures.stream().filter(Future::isDone).forEach(ParallelTableMapping::await);
                }
            } catch (final InterruptedException e) {
                throw ParallelTableMapping.unchecked(e);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T> void drain(final BlockingQueue<Object> queue, final Consumer<? super T> action) {
            try {
                Object item;
                while ((item = queue.take()) != ParallelTableMapping.END_OF_ITEMS) {
                    action.accept((T) item);
                }
            } catch (final InterruptedException e) {
                throw ParallelTableMapping.unchecked(e);
            }
        }
    }

    /**
     * threads個の作業スレッドでworker(i)を実行し、全ての結果を返す。投入元の文脈を引き継ぎ、
     * いずれかが失敗したら残りを中断して例外を投げる。戻る前に作業スレッドの終了を待つ
     */
    private static <R> List<R> runWorkers(final int threads, final String threadNamePrefix
            , final IntFunction<Callable<R>> worker) {
        final ExecutorService executor = ParallelTableMapping.newExecutor(threads, threadNamePrefix);
        // executeは投入元のスレッドで呼ばれるため、ここで投入元の文脈を包む
        final CompletionService<R> completion = new ExecutorCompletionService<>(
                command -> executor.execute(ThreadContexts.propagate(command)));
        final List<Future<R>> futures = IntStream.range(0, threads)
                .mapToObj(i -> completion.submit(worker.apply(i)))
                .toList();
        final List<R> result = new ArrayList<>();
        try {
            for (int i = 0; i < threads; i++) {
                result.add(ParallelTableMapping.await(completion.take()));
            }
        } catch (final InterruptedException e) {
            futures.forEach(it -> it.cancel(true));
            throw ParallelTableMapping.unchecked(e);
        } catch (final RuntimeException | Error e) {
            futures.forEach(it -> it.cancel(true));
            throw e;
        } finally {
            ParallelTableMapping.shutdown(executor);
        }
        return result;
    }

    /**
     * 作業スレッドの結果を待つ。失敗していればその原因を、割り込まれたら中断を投げる
     */
    private static <R> R await(final Future<R> future) {
        try {
            return future.get();
        } catch (final ExecutionException e) {
            throw ParallelTableMapping.rethrow(e.getCause());
        } catch (final InterruptedException e) {
            throw ParallelTableMapping.unchecked(e);
        }
    }

    /**
     * 割り込みを非検査例外にする（割り込み状態は戻す）
     */
    private static CancellationException unchecked(final InterruptedException e) {
        Thread.currentThread().interrupt();
        return new CancellationException("interrupted");
    }

    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("interrupted");
        }
    }

    /**
     * @return 並列数。paramで未指定（null・0以下）ならCPUコア数
     */
    public static int parallelism(final ComparableDataSetParam param) {
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
        return ParallelTableMapping.groupByShared(tasks, outputTables);
    }

    /**
     * keysを1つでも共有する要素を同じグループにまとめる（連結成分）。グループ内・グループ間とも元の順を保つ
     *
     * @param keys itemsと同じ並びで、各要素のkey
     */
    public static <T> List<List<T>> groupByShared(final List<T> items, final List<? extends Collection<?>> keys) {
        final int[] parent = new int[items.size()];
        final Map<Object, Integer> firstOwner = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            parent[i] = i;
            for (final Object key : keys.get(i)) {
                final Integer other = firstOwner.putIfAbsent(key, i);
                if (other != null) {
                    ParallelTableMapping.union(parent, other, i);
                }
            }
        }
        final Map<Integer, List<T>> result = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            result.computeIfAbsent(ParallelTableMapping.find(parent, i), k -> new ArrayList<>()).add(items.get(i));
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
                    ParallelTableMapping.checkInterrupted();
                    (first ? task : task.forWorker(resources)).run(context);
                }
            }
            return context.close();
        }
    }

    private static ExecutorService newExecutor(final int threads, final String threadNamePrefix) {
        return Executors.newFixedThreadPool(threads, runnable -> {
            final Thread thread = new Thread(runnable, threadNamePrefix + ParallelTableMapping.THREAD_NO.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @FunctionalInterface
    public interface WorkerAction<T> {
        void accept(T item, WorkerResources resources, boolean first);
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
