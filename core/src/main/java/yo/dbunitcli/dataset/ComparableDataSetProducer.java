package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.io.File;

public interface ComparableDataSetProducer extends IDataSetProducer {

    default String getSrc() {
        return this.getParam().getSrc().getPath();
    }

    ComparableDataSetParam getParam();

    default ITableMetaData createMetaData(File aFile, String[] header) {
        Column[] columns = new Column[header.length];
        for (int i = 0; i < header.length; i++) {
            columns[i] = new Column(header[i].trim(), DataType.UNKNOWN);
        }
        return this.createMetaData(aFile, columns);
    }

    default  ITableMetaData createMetaData(File aFile, Column[] columns) {
        return new DefaultTableMetaData(this.getTableName(aFile), columns);
    }


    default String getTableName(File aFile) {
        return aFile.getName().substring(0, aFile.getName().lastIndexOf("."));
    }

}
