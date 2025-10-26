package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;

import java.io.File;
import java.util.stream.Stream;

public interface ComparableDataSetProducer {

    Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetProducer.class);

    ComparableDataSetParam param();

    default String getSrc() {
        return this.param().src().getPath();
    }

    default void produce(final ComparableDataSetConsumer consumer) throws DataSetException {
        LOGGER.info("produce() - start");
        consumer.startDataSet();
        this.getSourceStream()
                .map(it -> this.createExecuteTableTask(it, consumer))
                .forEach(Runnable::run);
        consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    default Stream<Source> getSourceStream() {
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

    Runnable createExecuteTableTask(final Source source, ComparableDataSetConsumer consumer);

}
