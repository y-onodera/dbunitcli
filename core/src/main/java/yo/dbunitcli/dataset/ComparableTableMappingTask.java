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
        return new WithTargetTable(tableName, List.of(this));
    }

    /**
     * @param tasks runで順に実行するタスク（1つ以上）。テーブルtargetTableNameに行を入れるタスク
     */
    record WithTargetTable(String targetTableName,
                           List<ComparableTableMappingTask> tasks) implements ComparableTableMappingTask {

        public WithTargetTable {
            if (tasks.isEmpty()) {
                throw new IllegalArgumentException("tasks is empty. targetTableName:" + targetTableName);
            }
            tasks = List.copyOf(tasks);
        }

        @Override
        public Source source() {
            return this.tasks.getFirst().source();
        }

        public ComparableDataSetParam param() {
            return this.tasks.getFirst().param();
        }

        /**
         * タスクを同じコンテキストで順に1回ずつ実行する。同じテーブルに入る2つ目以降のタスクの行は、
         * 前のタスクが書いた続きから書き込み・分割する（{@link WrittenTables}）
         */
        public void run(final ComparableTableMappingContext mappingContext) {
            this.tasks.forEach(task -> task.run(mappingContext));
        }

        @Override
        public Stream<TableMetaDataWithSource> outputTableCandidates() {
            return this.tasks.stream().flatMap(ComparableTableMappingTask::outputTableCandidates);
        }

        @Override
        public ComparableTableMappingTask.WithTargetTable with(
                final Consumer<ComparableDataSetParam.Builder> consumer) {
            return new WithTargetTable(this.targetTableName, this.tasks.stream().map(it -> it.with(consumer)).toList());
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return this.tasks.getFirst().with(builder);
        }

        @Override
        public WithTargetTable forWorker(final Workers.WorkerResources resources) {
            return new WithTargetTable(this.targetTableName, this.tasks.stream().map(it -> it.forWorker(resources)).toList());
        }
    }
}
