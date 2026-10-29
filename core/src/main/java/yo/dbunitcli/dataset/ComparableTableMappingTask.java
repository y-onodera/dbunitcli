package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.resource.Workers;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ComparableTableMappingTask {

    Source source();

    ComparableDataSetParam param();

    default TableSeparators targetTableSeparators() {
        return this.param().tableSeparators();
    }

    void run(ComparableTableMappingContext context);

    /**
     * runで作るテーブルのメタデータ（列は空）。出力テーブルが同じタスクを同じスレッドへまとめるため、実行前に求める。
     * 実際に作るテーブルを漏らしてはならない（余分に含むのは可）
     */
    default Stream<TableMetaDataWithSource> outputTableCandidates() {
        return Stream.of(this.source().createMetaData(new Column[0]));
    }

    /**
     * 読込前に求めた出力テーブルの候補が、すべて出力の絞り込み（outputFilter）で除かれるか。除かれるソース・シートは読まずに済む。
     * 出力テーブル名が読込前に決まらない設定（join・件数分割）や、絞り込みが無い場合は常にfalse
     */
    default boolean isFilteredOut(final Stream<TableMetaDataWithSource> candidates) {
        final TableSeparators separators = this.targetTableSeparators();
        return separators.hasOutputFilter()
                && separators.isIndependentTables()
                && candidates.flatMap(separators::getAddSettingTableMetaData).findAny().isEmpty();
    }

    /**
     * 並列実行で2つ目以降の作業スレッドが使うタスクを返す。スレッド間で共有できない資源（DB接続など）は
     * resourcesからそのスレッド専用のものを取得して差し替える
     */
    default ComparableTableMappingTask forWorker(final Workers.WorkerResources resources) {
        return this;
    }

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
        public Stream<TableMetaDataWithSource> outputTableCandidates() {
            return this.tasks().flatMap(ComparableTableMappingTask::outputTableCandidates);
        }

        /**
         * runで順に実行するタスク
         */
        public Stream<ComparableTableMappingTask> tasks() {
            return Stream.concat(Stream.of(this.head), this.rest.stream());
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

        @Override
        public WithTargetTable forWorker(final Workers.WorkerResources resources) {
            return new WithTargetTable(this.targetTableName, this.head.forWorker(resources),
                                       this.rest.stream().map(it -> it.forWorker(resources)).toList());
        }
    }
}
