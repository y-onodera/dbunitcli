package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ComparableDataSetProducer extends AutoCloseable {

    Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetProducer.class);

    /**
     * 読込に使った資源（DB接続など）を解放する。作った側が読込の完了後（遅延読込なら出力の完了後）に呼ぶ
     */
    @Override
    default void close() {
        // nothing default
    }

    default ComparableDataSet loadDataSet() {
        return new ComparableDataSet(this.param(), this.produce(), this.getSrc());
    }

    ComparableDataSetParam param();

    default String getSrc() {
        return this.param().src().getPath();
    }

    default TreeMap<String, ComparableTable> produce() {
        LOGGER.info("produce() - start");
        final Stream<ComparableTableMappingTask> tasks = this.getSourceStream().map(this::createTableMappingTask);
        final TreeMap<String, ComparableTable> result;
        if (ParallelTableMapping.isParallelizable(this)) {
            // レーン分けには全タスクが要るため、並列化の余地があるときだけ先に列挙する。除外もレーン分けと同じ候補で行う
            final ParallelTableMapping.Plan plan = ParallelTableMapping.plan(this, tasks.collect(Collectors.toList()));
            result = plan.lanes()
                    .map(lanes -> ParallelTableMapping.run(this.param(), lanes))
                    .orElseGet(() -> this.produceSequential(plan.tasks().stream()));
        } else {
            result = this.produceSequential(this.excludeFilteredOut(tasks));
        }
        LOGGER.info("produce() - end");
        return result;
    }

    /**
     * 出力テーブル名が読込前に決まる形式（{@link #isParallelizableSource()}）で、そのすべてが出力の絞り込み（outputFilter）で
     * 除かれるソースは読まずに除外する（対象外のファイルを開かない・DBにクエリを発行しない）
     */
    private Stream<ComparableTableMappingTask> excludeFilteredOut(final Stream<ComparableTableMappingTask> tasks) {
        final TableSeparators tableSeparators = this.param().tableSeparators();
        if (!this.isParallelizableSource() || !tableSeparators.canExcludeBeforeRead()) {
            return tasks;
        }
        return tasks.filter(task -> !tableSeparators.excludesAll(task.outputTableCandidates()));
    }

    private TreeMap<String, ComparableTable> produceSequential(final Stream<ComparableTableMappingTask> tasks) {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(this.param().tableSeparators(), this.param().converter());
        context.open();
        tasks.forEach(it -> it.run(context));
        return context.close();
    }

    /**
     * ソースごとのタスクを別スレッドで同時に実行できるか。
     * 出力テーブル名がソースの読込前に決まり（{@link yo.dbunitcli.common.Source#getTableName()}）、タスク間で資源を共有しない場合のみtrueにする
     */
    default boolean isParallelizableSource() {
        return false;
    }

    default Map<String, Object> lazyLoad(final boolean includeMetaData) {
        final Map<String, Object> result = new LinkedHashMap<>();
        LOGGER.info("lazyLoad() - start, includeMetaData={}", includeMetaData);
        this.lazyLoadTables()
                .forEach(it -> {
                    LOGGER.info("lazyLoad() - processing tableName={}", it.getTableName());
                    if (includeMetaData) {
                        result.put(it.getTableName(), it);
                    } else {
                        result.put(it.getTableName(), it.getTask());
                    }
                });
        LOGGER.info("lazyLoad() - end, result size={}", result.size());
        return result;
    }

    /**
     * produce()と同じ条件で、並列化できればレーンごとに並列に求める
     */
    private Collection<ComparableTableDto> lazyLoadTables() {
        final Stream<ComparableTableMappingTask> tasks = this.getSourceStream().map(this::createTableMappingTask);
        if (!ParallelTableMapping.isParallelizable(this)) {
            return LazyloadTaskMapper.mappingFrom(this.param(), this.excludeFilteredOut(tasks));
        }
        final ParallelTableMapping.Plan plan = ParallelTableMapping.plan(this, tasks.collect(Collectors.toList()));
        return plan.lanes()
                   .map(lanes -> LazyloadTaskMapper.mappingFrom(this.param(), lanes))
                   .orElseGet(() -> LazyloadTaskMapper.mappingFrom(this.param(), plan.tasks().stream()));
    }

    default Stream<? extends Source> getSourceStream() {
        return this.getSrcFiles()
                .map(this::getSource)
                .filter(it -> this.param().tableNameFilter().predicate(it.getTableName()));
    }

    default Stream<File> getSrcFiles() {
        return this.param().getSrcFiles();
    }

    default Source getSource(final File aFile) {
        return new Source(aFile, this.param().addFileInfo());
    }

    ComparableTableMappingTask createTableMappingTask(final Source source);

}
