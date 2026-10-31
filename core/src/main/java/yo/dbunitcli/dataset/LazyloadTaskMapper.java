package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.resource.Workers;

import java.util.*;
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
        final RecordingTableMap tableMap = new RecordingTableMap();
        final ComparableTableMappingContext context = new ComparableTableMappingContext(tableSeparators, collector, tableMap, new WrittenTables(), joins, new ArrayList<>(), false);
        final Map<String, ComparableTableMappingTask.WithTargetTable> tableTasks = new HashMap<>();
        context.open();
        tasks.forEach(task -> {
            Workers.checkInterrupted();
            final ComparableTableMappingTask loading = task.with(builder -> builder.setLoadData(loadRows));
            (resources == null ? loading : loading.forWorker(resources)).run(context);
            // 行を処理しながら出力したテーブル（collector）と、読込の終わりにまとめて出力するテーブル（tableMap）
            Stream.concat(LazyloadTaskMapper.drain(collector.tableNames), LazyloadTaskMapper.drain(tableMap.putKeys))
                  .distinct()
                  .forEach(tableName -> tableTasks.merge(tableName, task.withTargetTable(tableName)
                          , ComparableTableMappingTask.WithTargetTable::chain));
        });
        final Map<String, ITableMetaData> output = collector.takeMetaData();
        context.close();
        // 読込の終わりに出力したテーブルのうち、ソースのタスクで出していないものはjoinの出力
        final Map<String, ITableMetaData> closed = collector.takeMetaData();
        output.putAll(closed);
        final Set<String> joinOutputs = new LinkedHashSet<>(closed.keySet());
        joinOutputs.removeAll(tableTasks.keySet());
        joins.forEach(join -> this.joinTask(join, tableTasks).ifPresent(task -> {
            final Set<String> outputOfJoin = LazyloadTaskMapper.outputOf(context, join, joinOutputs);
            joinOutputs.stream()
                       .filter(outputOfJoin::contains)
                       .forEach(tableName -> tableTasks.putIfAbsent(tableName, task.retarget(tableName)));
        }));
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

    private static Stream<String> drain(final Set<String> tableNames) {
        final List<String> result = new ArrayList<>(tableNames);
        tableNames.clear();
        return result.stream();
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
     * candidatesのうちjoinの出力テーブル（分割していれば分割後のテーブル）。分割後の名前は番号順に、candidatesに無くなるまで求める
     */
    private static Set<String> outputOf(final ComparableTableMappingContext context, final ComparableTableJoin join
            , final Set<String> candidates) {
        final Set<String> result = new HashSet<>();
        if (!join.isExecutable()) {
            return result;
        }
        context.getAddSettingTableMetaData(join).forEach(metaData -> {
            final TableSplitter splitter = metaData.getTableSplitter();
            if (!splitter.isSplit()) {
                result.add(metaData.getTableName());
                return;
            }
            for (int no = 0; candidates.contains(splitter.tableName(metaData.getTableName(), no)); no++) {
                result.add(splitter.tableName(metaData.getTableName(), no));
            }
        });
        return result;
    }

    /**
     * 出力されたテーブルのメタデータを受け取る（行データは使わない）。
     * 行は分割後の名前を求めるのに要るテーブル（分割するテーブル・分割するjoinに関係するテーブル）だけ読む
     */
    private static final class MetaDataCollector extends MetaDataCapture {

        private final Map<String, ITableMetaData> metaData = new LinkedHashMap<>();
        private final Set<String> tableNames = new LinkedHashSet<>();
        private final boolean joinRowRequired;

        private MetaDataCollector(final boolean joinRowRequired) {
            this.joinRowRequired = joinRowRequired;
        }

        @Override
        public boolean isRowRequired(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
            return metaData.getTableSplitter().isSplit() || (this.joinRowRequired && !joins.isEmpty());
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
    }

    /**
     * タスクごとに、読込の終わりにまとめて出力するテーブル（distinct・order・join・分割で行を保持するもの）の名前を記録する
     */
    private static final class RecordingTableMap extends TreeMap<String, ComparableTable> {

        private final Set<String> putKeys = new LinkedHashSet<>();

        @Override
        public ComparableTable put(final String key, final ComparableTable value) {
            this.putKeys.add(key);
            return super.put(key, value);
        }
    }
}
