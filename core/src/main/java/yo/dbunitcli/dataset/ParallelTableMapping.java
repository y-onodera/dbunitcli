package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.resource.Workers;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        Workers.run(threads, "produce-", i -> () -> ParallelTableMapping.runWorker(param, queue, i == 0))
                .forEach(result::putAll);
        return result;
    }

    /**
     * @return paramの並列数。未指定なら{@link Workers#defaultParallelism()}（CPUコア数。作業スレッドの中では1）
     */
    public static int parallelism(final ComparableDataSetParam param) {
        return Workers.parallelism(param.parallel());
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
        return Workers.groupByShared(tasks, outputTables);
    }

    /**
     * キューが空になるまでレーンを取り出して処理する。converter・DB接続はワーカー単位で確保してレーン間で使い回す。
     * レーンごとに出力テーブルが異なるので、コンテキストもワーカーで1つにできる
     *
     * @param first 最初のワーカーは元のconverterとタスクをそのまま使う（DB接続を余分に張らない）
     */
    private static TreeMap<String, ComparableTable> runWorker(final ComparableDataSetParam param
            , final Queue<List<ComparableTableMappingTask>> queue, final boolean first) {
        try (final Workers.WorkerResources resources = new Workers.WorkerResources()) {
            final IDataSetConverter converter = first || param.converter() == null
                    ? param.converter()
                    : param.converter().splitForParallel(resources);
            final ComparableTableMappingContext context = new ComparableTableMappingContext(param.tableSeparators(), converter);
            context.open();
            List<ComparableTableMappingTask> lane;
            while ((lane = queue.poll()) != null) {
                for (final ComparableTableMappingTask task : lane) {
                    Workers.checkInterrupted();
                    (first ? task : task.forWorker(resources)).run(context);
                }
            }
            return context.close();
        }
    }
}
