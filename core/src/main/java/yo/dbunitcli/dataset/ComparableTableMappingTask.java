package yo.dbunitcli.dataset;

import yo.dbunitcli.common.Source;

import java.util.*;
import java.util.function.Consumer;

public interface ComparableTableMappingTask {

    Source source();

    ComparableDataSetParam param();

    default TableSeparators targetTableSeparators() {
        return this.param().tableSeparators();
    }

    void run(ComparableTableMappingContext context);

    default TreeMap<String, ComparableTable> runWith(final Consumer<TableSeparators.Builder> editor,
                                                     IDataSetConverter converter) {
        ComparableTableMappingContext context = new ComparableTableMappingContext(
                this.targetTableSeparators().map(editor)
                , converter);
        context.open();
        this.run(context);
        return context.close();
    }

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
        public ComparableTableMappingTask.WithTargetTable with(
                final Consumer<ComparableDataSetParam.Builder> consumer) {
            return new WithTargetTable(this.targetTableName, this.head().with(consumer),
                                       this.rest.stream().map(it -> it.with(consumer)).toList());
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return this.head().with(builder);
        }

        public WithTargetTable chain(final WithTargetTable target) {
            final List<ComparableTableMappingTask> newRest = new ArrayList<>(this.rest);
            newRest.add(target.head());
            newRest.addAll(target.rest());
            return new WithTargetTable(this.targetTableName(), this.head(), newRest);
        }
    }
}
