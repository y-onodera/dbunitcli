package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.io.File;
import java.io.IOException;

public interface IDataSetConverter extends org.dbunit.dataset.stream.IDataSetConsumer {

    void cleanupDirectory() throws DataSetException;

    boolean isExportEmptyTable();

    void reStartTable(ITableMetaData tableMetaData, Integer writeRows) throws DataSetException;

    default void write(ITable aTable) throws DataSetException {
        if (!this.isExportEmptyTable() && aTable.getRowCount() == 0) {
            return;
        }
        this.startTable(aTable.getTableMetaData());
        Column[] columns = aTable.getTableMetaData().getColumns();
        for (int i = 0, j = aTable.getRowCount(); i < j; i++) {
            Object[] row = new Object[columns.length];
            for (int index = 0, last = columns.length; index < last; index++) {
                row[index] = aTable.getValue(i, columns[index].getColumnName());
            }
            this.row(row);
        }
        this.endTable();
    }

    default File getDir() {
        return null;
    }

}
