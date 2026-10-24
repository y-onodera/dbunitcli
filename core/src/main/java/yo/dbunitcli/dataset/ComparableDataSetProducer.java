package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ComparableDataSetProducer {

    Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetProducer.class);

    default ComparableDataSet loadDataSet() {
        return new ComparableDataSet(this.param(), this.produce(), this.getSrc());
    }

    ComparableDataSetParam param();

    default String getSrc() {
        return this.param().src().getPath();
    }

    default TreeMap<String, ComparableTable> produce() {
        LOGGER.info("produce() - start");
        final List<ComparableTableMappingTask> tasks = this.getSourceStream()
                .map(this::createTableMappingTask)
                .collect(Collectors.toList());
        final TreeMap<String, ComparableTable> result = ParallelTableMapping.lanes(this, tasks)
                .map(lanes -> ParallelTableMapping.run(this.param(), lanes))
                .orElseGet(() -> {
                    final ComparableTableMappingContext context = new ComparableTableMappingContext(this.param().tableSeparators(), this.param().converter());
                    context.open();
                    tasks.forEach(it -> it.run(context));
                    return context.close();
                });
        LOGGER.info("produce() - end");
        return result;
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
        LazyloadTaskMapper.mappingFrom(this.param(), this.getSourceStream().map(this::createTableMappingTask))
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
