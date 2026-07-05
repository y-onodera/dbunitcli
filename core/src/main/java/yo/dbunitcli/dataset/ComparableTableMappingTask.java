package yo.dbunitcli.dataset;

import yo.dbunitcli.common.Source;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public interface ComparableTableMappingTask {

    Source source();

    ComparableDataSetParam param();

    void run(ComparableTableMappingContext context);

    default ComparableTableMappingTask with(final Consumer<ComparableDataSetParam.Builder> consumer) {
        final ComparableDataSetParam.Builder builder = this.param().toBuilder();
        consumer.accept(builder);
        return this.with(builder);
    }

    ComparableTableMappingTask with(ComparableDataSetParam.Builder builder);

    default WithTargetTable withTargetTable(final String tableName) {
        return new WithTargetTable(tableName, this);
    }

    record WithTargetTable(String targetTableName,
                           ComparableTableMappingTask head,
                           List<ComparableTableMappingTask> rest) implements ComparableTableMappingTask {

        public WithTargetTable(final String tableName, final ComparableTableMappingTask head) {
            this(tableName, head, List.of());
        }

        @Override
        public Source source() {
            return this.head.source();
        }

        public ComparableDataSetParam param() {
            return this.head.param();
        }

        public void run(final ComparableTableMappingContext mappingContext) {
            this.head().run(mappingContext.addChain(this.rest()));
        }

        @Override
        public ComparableTableMappingTask with(final Consumer<ComparableDataSetParam.Builder> consumer) {
            return new WithTargetTable(this.targetTableName, this.head().with(consumer), this.rest.stream().map(it -> it.with(consumer)).toList());
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return this.head().with(builder);
        }

        public TableSeparators targetTableSeparators() {
            return this.param().tableSeparators();
        }

        public List<Map<String, Object>> resolveRows(final TableSeparators tableSeparators) {
            final TableRowsCollector collector = new TableRowsCollector(this.targetTableName());
            this.runWith(new ComparableTableMappingContext(tableSeparators, collector)).close();
            return collector.getRows();
        }

        public Map<String, Object> resolveDataSet(final TableSeparators tableSeparators) {
            final TreeMap<String, ComparableTable> tableMap = this.runWith(new ComparableTableMappingContext(tableSeparators, null)).close();
            final Map<String, Object> result = new LinkedHashMap<>();
            // each ComparableTable.toMap(true) is a one-element list holding the table's own metadata+rows wrapper
            tableMap.forEach((tableName, table) -> result.put(tableName, table.toMap(true).getFirst()));
            return result;
        }

        private ComparableTableMappingContext runWith(final ComparableTableMappingContext context) {
            context.open();
            this.run(context);
            return context;
        }

        public WithTargetTable chain(final WithTargetTable target) {
            final List<ComparableTableMappingTask> newRest = new ArrayList<>(this.rest);
            newRest.add(target.head());
            newRest.addAll(target.rest());
            return new WithTargetTable(this.targetTableName(), this.head(), newRest);
        }
    }
}
