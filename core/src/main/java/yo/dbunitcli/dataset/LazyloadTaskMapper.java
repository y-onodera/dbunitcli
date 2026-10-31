package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.resource.Workers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 出力されるテーブルごとに、そのテーブルを読み込むタスクを求める。
 * 出力されるテーブルはlazyLoadしない読込と同じ処理（join・分割・distinct・order）で求め、行は分割後の名前を求める場合だけ読む
 */
public class LazyloadTaskMapper {

    private final Map<String, ComparableTableDto> result = new TreeMap<>();

    public static Collection<ComparableTableDto> mappingFrom(final ComparableDataSetParam param, final Stream<ComparableTableMappingTask> tasks) {
        final LazyloadTaskMapper mapper = new LazyloadTaskMapper(param, tasks, null);
        return mapper.result.values();
    }

    /**
     * レーン（{@link ParallelTableMapping#plan}）ごとに別スレッドで求める。レーン間で出力テーブルを共有しないため、結果はそのまま合わせられる
     */
    public static Collection<ComparableTableDto> mappingFrom(final ComparableDataSetParam param
            , final List<List<ComparableTableMappingTask>> lanes) {
        final Map<String, ComparableTableDto> result = new TreeMap<>();
        ParallelTableMapping.runLanes(param, lanes, "lazyload-", (tasks, resources) -> new LazyloadTaskMapper(param, tasks, resources).result)
                            .forEach(result::putAll);
        return result.values();
    }

    /**
     * @param resources 作業スレッドの資源。元のタスクのまま読む場合はnull（結果のタスクは常に元のタスクを使う）
     */
    private LazyloadTaskMapper(final ComparableDataSetParam param, final Stream<ComparableTableMappingTask> tasks
            , final Workers.WorkerResources resources) {
        final TableSeparators tableSeparators = param.tableSeparators();
        final boolean loadRows = tableSeparators.isTableNameDependsOnRows();
        final List<ComparableTableJoin> joins = tableSeparators.joins();
        final MetaDataCollector collector = new MetaDataCollector(LazyloadTaskMapper.isJoinRowRequired(tableSeparators));
        final WrittenTables written = new WrittenTables();
        final ComparableTableMappingContext context = new ComparableTableMappingContext(tableSeparators, collector
                , new TreeMap<>(), written, joins, new ArrayList<>(), false, collector);
        final Map<String, ComparableTableMappingTask.WithTargetTable> tableTasks = new LinkedHashMap<>();
        context.open();
        tasks.forEach(task -> {
            Workers.checkInterrupted();
            final ComparableTableMappingTask loading = task.with(builder -> builder.setLoadData(loadRows));
            (resources == null ? loading : loading.forWorker(resources)).run(context);
            collector.drainTableNames()
                     .forEach(tableName -> tableTasks.merge(tableName, task.withTargetTable(tableName)
                             , ComparableTableMappingTask.WithTargetTable::chain));
        });
        LazyloadTaskMapper.readSplitGroupFromAllSources(tableTasks, written);
        final Map<String, ITableMetaData> output = collector.takeMetaData();
        context.close();
        output.putAll(collector.takeMetaData());
        // joinを実行した順に、joinの出力テーブルを左右のテーブルを読むタスクで読む（joinの結果を使うjoinは、使うjoinのタスクも含む）
        collector.joinOutputs.forEach((join, tableNames) -> this.joinTask(join, tableTasks)
                .ifPresent(task -> tableNames.forEach(tableName -> tableTasks.putIfAbsent(tableName, task.retarget(tableName)))));
        output.forEach((tableName, metaData) -> {
            final ComparableTableMappingTask.WithTargetTable task = tableTasks.get(tableName);
            if (task != null) {
                final ComparableTableDto dto = new ComparableTableDto(metaData);
                dto.setRows(task);
                this.result.put(tableName, dto);
            }
        });
    }

    /**
     * joinの結果を分割する（joinの設定・後段（next）に分割がある）場合は、joinに関係するテーブルの行を読む
     */
    private static boolean isJoinRowRequired(final TableSeparators tableSeparators) {
        return tableSeparators.hasJoinSplitter()
                || (tableSeparators.next() != null && tableSeparators.next().isTableNameDependsOnRows());
    }

    /**
     * 複数のソースから行が入る分割では、分割後のテーブルはどれも、そのまとまりに書いた全ソースから読む。
     * 分割はソースをまたいで数えるため、一部のソースだけを読むと分割後のテーブルの行が変わる
     */
    private static void readSplitGroupFromAllSources(final Map<String, ComparableTableMappingTask.WithTargetTable> tableTasks
            , final WrittenTables written) {
        final Map<String, List<String>> groups = new LinkedHashMap<>();
        tableTasks.keySet().forEach(tableName -> written.splitGroup(tableName)
                                                         .ifPresent(group -> groups.computeIfAbsent(group, _ -> new ArrayList<>()).add(tableName)));
        groups.values().forEach(tableNames -> {
            final List<ComparableTableMappingTask> tasks = tableNames.stream()
                                                                     .flatMap(tableName -> tableTasks.get(tableName).tasks())
                                                                     .distinct()
                                                                     .toList();
            tableNames.forEach(tableName -> tableTasks.put(tableName, new ComparableTableMappingTask.WithTargetTable(tableName
                    , tasks.getFirst(), tasks.subList(1, tasks.size()))));
        });
    }

    /**
     * joinの左右のテーブルを読み込むタスクを順につなげる（同じソースは1回だけ読む）
     */
    private Optional<ComparableTableMappingTask.WithTargetTable> joinTask(final ComparableTableJoin join
            , final Map<String, ComparableTableMappingTask.WithTargetTable> tableTasks) {
        return tableTasks.entrySet().stream()
                         .filter(it -> join.hasRelation(it.getKey()))
                         .map(Map.Entry::getValue)
                         .reduce((old, v) -> old.source().equals(v.source()) ? old : old.chain(v));
    }

    /**
     * 出力されたテーブルのメタデータを受け取る（行データは使わない）。
     * 行は分割後の名前を求めるのに要るテーブル（分割するテーブル・分割するjoinに関係するテーブル）だけ読む
     */
    private static final class MetaDataCollector extends MetaDataCapture implements ComparableTableMappingContext.Listener {

        private final Map<String, ITableMetaData> metaData = new LinkedHashMap<>();
        /**
         * 前回取り出してから出力したテーブル。行を処理しながら出力したもの（startTable）と、読込の終わりにまとめて出力するもの（held）
         */
        private final Set<String> tableNames = new LinkedHashSet<>();
        /**
         * 実行した順のjoinごとの出力テーブル
         */
        private final Map<ComparableTableJoin, Set<String>> joinOutputs = new LinkedHashMap<>();
        private final boolean joinRowRequired;

        private MetaDataCollector(final boolean joinRowRequired) {
            this.joinRowRequired = joinRowRequired;
        }

        @Override
        public boolean isRowRequired(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
            return metaData.getTableSplitter().isSplit() || (this.joinRowRequired && !joins.isEmpty());
        }

        Stream<String> drainTableNames() {
            final List<String> result = new ArrayList<>(this.tableNames);
            this.tableNames.clear();
            return result.stream();
        }

        Map<String, ITableMetaData> takeMetaData() {
            final Map<String, ITableMetaData> result = new LinkedHashMap<>(this.metaData);
            this.metaData.clear();
            return result;
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.metaData.putIfAbsent(metaData.getTableName(), metaData);
            this.tableNames.add(metaData.getTableName());
        }

        @Override
        public void held(final String tableName) {
            this.tableNames.add(tableName);
        }

        @Override
        public void joined(final ComparableTableJoin join) {
            this.joinOutputs.put(join, this.drainTableNames().collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }
}
