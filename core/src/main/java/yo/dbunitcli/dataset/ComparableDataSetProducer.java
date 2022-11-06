package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.io.File;
import java.util.Arrays;

public interface ComparableDataSetProducer extends IDataSetProducer {

    default String getSrc() {
        return this.getParam().getSrc().getPath();
    }

    ComparableDataSetParam getParam();

    default ITableMetaData createMetaData(final File aFile, final String[] header) {
        return this.createMetaData(aFile, Arrays.stream(header).map(s -> new Column(s.trim(), DataType.UNKNOWN))
                .toArray(Column[]::new));
    }

    default ITableMetaData createMetaData(final File aFile, final Column[] columns) {
        return new DefaultTableMetaData(this.getTableName(aFile), columns);
    }

    default String getTableName(final File aFile) {
        return aFile.getName().substring(0, aFile.getName().lastIndexOf("."));
    }

}
