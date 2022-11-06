package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

public interface IDataSetConverter extends org.dbunit.dataset.stream.IDataSetConsumer {

    boolean isExportEmptyTable();

    void reStartTable(ITableMetaData tableMetaData, Integer writeRows) throws DataSetException;

    default void convert(final ITable aTable) {
        if (!this.isExportEmptyTable() && aTable.getRowCount() == 0) {
            return;
        }
        try {
            this.startTable(aTable.getTableMetaData());
            IntStream.range(0, aTable.getRowCount()).forEach(i -> this.convertRow(aTable, i));
            this.endTable();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default void convertRow(final ITable aTable, final int rowNum) {
        try {
            this.convertRow(Arrays.stream(aTable.getTableMetaData().getColumns())
                    .map(column -> this.getValue(aTable, rowNum, column))
                    .toArray(Object[]::new));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default void convertRow(final Object[] row) {
        try {
            this.row(row);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default Object getValue(final ITable aTable, final int i, final Column column) {
        try {
            return aTable.getValue(i, column.getColumnName());
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default File getDir() {
        return null;
    }

}
