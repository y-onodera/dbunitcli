package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.io.File;
import java.util.Arrays;

public interface ComparableDataSetProducer {

    void setConsumer(ComparableDataSetConsumer consumer) throws DataSetException;

    void produce() throws DataSetException;

    void executeTable(Source source);

    ComparableDataSetParam getParam();

    default String getSrc() {
        return this.getParam().src().getPath();
    }

    default Source getSource(final File aFile, final boolean addFileInfo) {
        return new Source(aFile, addFileInfo);
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
