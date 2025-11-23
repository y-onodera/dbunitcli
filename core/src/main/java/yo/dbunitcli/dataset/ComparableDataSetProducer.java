package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
        final ComparableTableMappingContext context = new ComparableTableMappingContext(this.param().tableSeparators(), this.param().converter());
        this.getSourceStream()
                .map(this::createTableMappingTask)
                .forEach(it -> it.run(context));
        LOGGER.info("produce() - end");
        return context.close();
    }

    default Map<String, Object> lazyLoad(final boolean includeMetaData) {
        final Map<String, Object> result = new HashMap<>();
        LOGGER.info("lazyLoad() - start, includeMetaData={}", includeMetaData);
        this.getSourceStream()
                .map(this::createTableMappingTask)
                .flatMap(task -> task.mappingTaskToTableName().stream())
                .forEach(it -> {
                    LOGGER.info("lazyLoad() - processing tableName={}", it.getTableName());
                    final Object current = result.get(it.getTableName());
                    if (includeMetaData) {
                        if (current instanceof final ComparableTableDto dto) {
                            LOGGER.info("lazyLoad() - chaining to existing DTO for table={}", it.getTableName());
                            final ComparableTableMappingTask.WithTargetTable task = dto.getTask();
                            dto.setRows(task.chain(it.getTask()));
                        } else {
                            LOGGER.info("lazyLoad() - creating new DTO for table={}", it.getTableName());
                            result.put(it.getTableName(), it);
                        }
                    } else {
                        if (current instanceof final ComparableTableMappingTask.WithTargetTable task) {
                            result.put(it.getTableName(), task.chain(it.getTask()));
                        } else {
                            result.put(it.getTableName(), it.getTask());
                        }
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
