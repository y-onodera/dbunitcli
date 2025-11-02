package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;

import java.io.File;
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
