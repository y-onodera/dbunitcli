package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public interface ComparableDataSetProducer {

    Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetProducer.class);

    void setConsumer(ComparableDataSetConsumer consumer) throws DataSetException;

    ComparableDataSetConsumer getConsumer();

    ComparableDataSetParam getParam();

    default String getSrc() {
        return this.getParam().src().getPath();
    }

    default boolean addFileInfo() {
        return this.getParam().addFileInfo();
    }

    default Stream<File> getSrcFiles() {
        return this.getParam().getSrcFiles();
    }

    default String getEncoding() {
        return this.getParam().encoding();
    }

    default String[] getHeaderNames() {
        return this.getParam().headerNames();
    }

    default boolean loadData() {
        return this.getParam().loadData();
    }

    default int getStartRow() {
        return this.getParam().startRow();
    }

    default Source getSource(final File aFile) {
        return new Source(aFile, this.addFileInfo());
    }

    default void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.getConsumer().startDataSet();
        this.getSourceStream()
                .map(this::createExecuteTableTask)
                .forEach(Runnable::run);
        this.getConsumer().endDataSet();
        LOGGER.info("produce() - end");
    }

    default Stream<Source> getSourceStream() {
        return this.getSrcFiles()
                .filter(it -> this.getParam().tableNameFilter().predicate(this.getTableName(it)))
                .map(this::getSource);
    }

    void executeTable(Source source);

    default Runnable createExecuteTableTask(final Source source) {
        return () -> this.executeTable(source);
    }

    default TableMetaDataWithSource createMetaData(final Source source, final String[] header) {
        try {
            return this.createMetaData(source, Arrays.stream(header).map(s -> new Column(s.trim(), DataType.UNKNOWN))
                    .toArray(Column[]::new));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create metadata with file info", e);
        }
    }

    default TableMetaDataWithSource createMetaData(final Source source, final Column[] columns) {
        try {
            return source
                    .wrap(new DefaultTableMetaData(this.getTableName(source.fileName()), columns));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create metadata with file info", e);
        }
    }

    default String getTableName(final File aFile) {
        return this.getTableName(aFile.getName());
    }

    default String getTableName(final String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

}
