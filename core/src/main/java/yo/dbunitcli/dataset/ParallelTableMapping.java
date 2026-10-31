package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.Workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * ソースを別スレッドで並列に読み込む。
 * 出力テーブルを共有するソースは同じレーンにまとめ、レーン内は元の順に逐次処理するため、
 * 同じテーブルへの追記順は逐次実行と変わらない。作業スレッド（ワーカー）ごとにコンテキスト・converter・DB接続を持ち、
 * ワーカーはレーンを順に取り出して処理するので、ワーカー間で状態を共有しない。
 */
public final class ParallelTableMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelTableMapping.class);

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
                && param.tableSeparators().isIndependentTables();
    }

    /**
     * 並列に読み込むレーンと、読込前に除外したあとのタスク
     *
     * @param tasks 除外したあとのタスク（レーンに分けない場合に逐次で読む）
     * @param lanes 並列化できる場合はレーンの一覧（2つ以上）。できない場合はempty
     */
    public record Plan(List<ComparableTableMappingTask> tasks, Optional<List<List<ComparableTableMappingTask>>> lanes) {
    }

    /**
     * 出力テーブルの候補をタスクごとに1回だけ求め、出力の絞り込み（outputFilter）ですべて除かれるタスクを除いてからレーンに分ける。
     * 候補はxls/xlsxでファイルを開くため、タスクごとに並列に求める
     */
    public static Plan plan(final ComparableDataSetProducer producer, final List<ComparableTableMappingTask> tasks) {
        if (!ParallelTableMapping.isParallelizable(producer)) {
            return new Plan(tasks, Optional.empty());
        }
        final TableSeparators tableSeparators = producer.param().tableSeparators();
        final List<List<String>> outputTables = tasks.parallelStream()
                .map(task -> task.outputTableCandidates()
                        .flatMap(tableSeparators::getAddSettingTableMetaData)
                        .map(AddSettingTableMetaData::getTableName)
                        .toList())
                .toList();
        final boolean exclude = tableSeparators.canExcludeBeforeRead();
        final List<ComparableTableMappingTask> keptTasks = new ArrayList<>();
        final List<List<String>> keptTables = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (!exclude || !outputTables.get(i).isEmpty()) {
                keptTasks.add(tasks.get(i));
                keptTables.add(outputTables.get(i));
            }
        }
        if (keptTasks.size() < 2) {
            return new Plan(keptTasks, Optional.empty());
        }
        // 出力テーブル名を1つでも共有するソースを同じレーンにまとめる（連結成分）。レーン内・レーン間とも元の順を保つ
        final List<List<ComparableTableMappingTask>> lanes = Workers.groupByShared(keptTasks, keptTables);
        return new Plan(keptTasks, lanes.size() < 2 ? Optional.empty() : Optional.of(lanes));
    }

    /**
     * @return 並列化できる場合はレーンの一覧（2つ以上）。できない場合はempty
     */
    public static Optional<List<List<ComparableTableMappingTask>>> lanes(final ComparableDataSetProducer producer
            , final List<ComparableTableMappingTask> tasks) {
        return ParallelTableMapping.plan(producer, tasks).lanes();
    }

    public static TreeMap<String, ComparableTable> run(final ComparableDataSetParam param
            , final List<List<ComparableTableMappingTask>> lanes) {
        final TreeMap<String, ComparableTable> result = new TreeMap<>();
        ParallelTableMapping.runLanes(param, lanes, "produce-", (tasks, resources) -> ParallelTableMapping.runWorker(param, tasks, resources))
                            .forEach(result::putAll);
        return result;
    }

    /**
     * レーンを処理する作業スレッドの中身
     */
    @FunctionalInterface
    public interface LaneWorker<R> {
        /**
         * @param tasks     この作業スレッドが取り出したレーンのタスク（元の順）
         * @param resources 作業スレッドの資源。最初の作業スレッドは元のconverter・タスクをそのまま使う（DB接続を余分に張らない）ためnull
         */
        R run(Stream<ComparableTableMappingTask> tasks, Workers.WorkerResources resources);
    }

    /**
     * 作業スレッドがキューが空になるまでレーンを取り出して順に処理し、作業スレッドごとの結果を返す
     */
    public static <R> List<R> runLanes(final ComparableDataSetParam param
            , final List<List<ComparableTableMappingTask>> lanes, final String threadNamePrefix, final LaneWorker<R> worker) {
        final int threads = Math.min(ParallelTableMapping.parallelism(param), lanes.size());
        ParallelTableMapping.LOGGER.info("runLanes() - {} parallel lanes={}, threads={}", threadNamePrefix, lanes.size(), threads);
        final Queue<List<ComparableTableMappingTask>> queue = new ConcurrentLinkedQueue<>(lanes);
        return Workers.run(threads, threadNamePrefix, i -> () -> {
            try (final Workers.WorkerResources resources = new Workers.WorkerResources()) {
                return worker.run(Stream.generate(queue::poll)
                                        .takeWhile(Objects::nonNull)
                                        .flatMap(List::stream)
                        , i == 0 ? null : resources);
            }
        });
    }

    /**
     * @return paramの並列数。未指定なら{@link Workers#defaultParallelism()}（CPUコア数。作業スレッドの中では1）
     */
    public static int parallelism(final ComparableDataSetParam param) {
        return Workers.parallelism(param.parallel());
    }

    /**
     * converter・DB接続はワーカー単位で確保してレーン間で使い回す。
     * レーンごとに出力テーブルが異なるので、コンテキストもワーカーで1つにできる
     */
    private static TreeMap<String, ComparableTable> runWorker(final ComparableDataSetParam param
            , final Stream<ComparableTableMappingTask> tasks, final Workers.WorkerResources resources) {
        final IDataSetConverter converter = resources == null || param.converter() == null
                ? param.converter()
                : param.converter().splitForParallel(resources);
        final ComparableTableMappingContext context = new ComparableTableMappingContext(param.tableSeparators(), converter);
        context.open();
        tasks.forEach(task -> {
            Workers.checkInterrupted();
            (resources == null ? task : task.forWorker(resources)).run(context);
        });
        return context.close();
    }
}
