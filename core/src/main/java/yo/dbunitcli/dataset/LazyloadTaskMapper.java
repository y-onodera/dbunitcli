package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.stream.Stream;

public class LazyloadTaskMapper {

    private final Map<String, ComparableTableDto> result;

    public static Collection<ComparableTableDto> mappingFrom(final ComparableDataSetParam param, final Stream<ComparableTableMappingTask> tasks) {
        final LazyloadTaskMapper mapper = new LazyloadTaskMapper(param, tasks);
        return mapper.result.values();
    }

    private LazyloadTaskMapper(final ComparableDataSetParam param, final Stream<ComparableTableMappingTask> tasks) {
        this.result = new HashMap<>();
        final TableSeparators tableSeparators = param.tableSeparators();
        final List<ComparableTableJoin> joins = tableSeparators.joins();
        final MetaDataCollector collector = new MetaDataCollector(joins);
        final ComparableTableMappingContext context = new ComparableTableMappingContext(tableSeparators, collector, new TreeMap<>(), new HashMap<>(), joins, new ArrayList<>(), false);
        context.open();
        tasks.forEach(task -> {
            task.with(builder -> builder.setLoadData(tableSeparators.hasSplitter()))
                    .run(context);
            collector.feedBackAndReset(this.result, task);
        });
        context.close();
        final Map<ComparableTableJoin, ComparableTableMappingTask.WithTargetTable> joinResult = new LinkedHashMap<>();
        joins.forEach(join -> this.result.forEach((key, value) -> {
            if (join.hasRelation(key)) {
                joinResult.merge(join
                        , value.getTask()
                        , (old, v) -> {
                            if (old.source().equals(v.source())) {
                                return old;
                            }
                            return old.chain(v);
                        });
            }
        }));
        joinResult.forEach((k, v) -> collector.feedBackJoin(this.result, context.getAddSettingTableMetaData(k).toList(), v));
    }

    private record MetaDataCollector(Set<ITableMetaData> items,
                                     Set<ITableMetaData> chains,
                                     List<ComparableTableJoin> joins) implements IDataSetConverter {

        private MetaDataCollector(final List<ComparableTableJoin> joins) {
            this(new LinkedHashSet<>(), new LinkedHashSet<>(), joins);
        }

        @Override
        public boolean isExportEmptyTable() {
            return true;
        }

        @Override
        public void reStartTable(final AddSettingTableMetaData metaData, final Integer writeRows) {
            this.chains.add(metaData);
            this.joins.forEach(it -> it.setIfRelated(new ComparableTable(new ComparableTable.Builder(metaData))));
        }

        @Override
        public IDataSetConverter split() {
            return this;
        }

        @Override
        public boolean isEnableRowProcessing(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
            return true;
        }

        @Override
        public void startTable(final ITableMetaData metaData) {
            this.items.add(metaData);
            this.joins.forEach(it -> it.setIfRelated(new ComparableTable(new ComparableTable.Builder(metaData))));
        }

        @Override
        public void endTable() {

        }

        @Override
        public void row(final Object[] values) {

        }

        public void feedBackAndReset(final Map<String, ComparableTableDto> result, final ComparableTableMappingTask task) {
            this.items.forEach(it -> result.put(it.getTableName(), new ComparableTableDto(it, task)));
            this.chains.forEach(it -> result.computeIfPresent(it.getTableName(), (key, value) -> {
                value.setRows(value.getTask().chain(task.withTargetTable(key)));
                return value;
            }));
            this.items.clear();
            this.chains.clear();
        }

        public void feedBackJoin(final Map<String, ComparableTableDto> result, final Collection<AddSettingTableMetaData> joinResult, final ComparableTableMappingTask task) {
            this.items.stream()
                    .filter(it -> joinResult.stream().map(AddSettingTableMetaData::getTableName).anyMatch(tableName -> tableName.equals(it.getTableName())))
                    .forEach(it -> result.put(it.getTableName(), new ComparableTableDto(it, task)));
        }
    }
}
