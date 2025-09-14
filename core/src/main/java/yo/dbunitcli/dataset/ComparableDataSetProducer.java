package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetProducer;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.io.File;
import java.util.Arrays;

public interface ComparableDataSetProducer extends IDataSetProducer {

    default String getSrc() {
        return this.getParam().src().getPath();
    }

    ComparableDataSetParam getParam();

    default TableMetaDataWithSource createMetaData(final File aFile, final String[] header, final boolean addFileInfo) {
        return this.createMetaData(aFile, Arrays.stream(header).map(s -> new Column(s.trim(), DataType.UNKNOWN))
                .toArray(Column[]::new), addFileInfo);
    }

    default TableMetaDataWithSource createMetaData(final File aFile, final Column[] columns, final boolean addFileInfo) {
        try {
            return this.getSource(aFile, addFileInfo)
                    .wrap(new DefaultTableMetaData(this.getTableName(aFile), columns));
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create metadata with file info", e);
        }
    }

    default Source getSource(final File aFile, final boolean addFileInfo) {
        return TableMetaDataWithSource.fileInfo(aFile, addFileInfo);
    }

    default String getTableName(final File aFile) {
        return aFile.getName().substring(0, aFile.getName().lastIndexOf("."));
    }

}
