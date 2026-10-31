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
        final MetaDataCollector collector = new MetaDataCollector(LazyloadTaskMapper.isJoinRowRequired(tableSeparators));
        final ComparableTableMappingContext context = new ComparableTableMappingContext(tableSeparators, collector);
        context.open();
        tasks.forEach(task -> {
            Workers.checkInterrupted();
            final ComparableTableMappingTask loading = task.with(builder -> builder.setLoadData(loadRows));
            // 出力テーブルごとに、行を入れたタスクとして元のタスクを記録する（読込にはloadDataなどを変えたタスクを使う）
            (resources == null ? loading : loading.forWorker(resources)).run(context.withSource(task));
        });
        context.close();
        collector.metaData.forEach((tableName, metaData) -> {
            final List<ComparableTableMappingTask> sources = context.written().sources(tableName);
            if (!sources.isEmpty()) {
                final ComparableTableDto dto = new ComparableTableDto(metaData);
                dto.setRows(new ComparableTableMappingTask.WithTargetTable(tableName, sources));
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
     * 出力されたテーブルのメタデータを受け取る（行データは使わない）。
     * 行は分割後の名前を求めるのに要るテーブル（分割するテーブル・分割するjoinに関係するテーブル）だけ読む
     */
    private static final class MetaDataCollector extends MetaDataCapture {

        private final Map<String, ITableMetaData> metaData = new LinkedHashMap<>();
        private final boolean joinRowRequired;

        private MetaDataCollector(final boolean joinRowRequired) {
            this.joinRowRequired = joinRowRequired;
        }

        @Override
        public boolean isRowRequired(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
            return metaData.getTableSplitter().isSplit() || (this.joinRowRequired && !joins.isEmpty());
        }

        /**
         * 並べ替え（order）しても出力テーブルの名前は変わらないため、distinct・breakKeyでの分割・joinが無ければ
         * 行を保持せず流しながら名前を求める（分割は件数だけで決まる）
         */
        @Override
        public boolean isEnableRowProcessing(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
            return !metaData.isNeedDistinct() && metaData.getTableSplitter().breakKeys().isEmpty() && joins.isEmpty();
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.metaData.putIfAbsent(metaData.getTableName(), metaData);
        }
    }
}
