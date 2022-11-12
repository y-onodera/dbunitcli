package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.util.List;

public class TableCompare {
    private final ComparableTable oldTable;
    private final ComparableTable newTable;
    private final AddSettingColumns comparisonKeys;
    private final IDataSetConverter converter;
    private final int columnLength;
    private final List<String> keyColumns;

    public TableCompare(final ComparableTable oldTable, final ComparableTable newTable, final AddSettingColumns comparisonKeys, final IDataSetConverter converter) {
        this.oldTable = oldTable;
        this.newTable = newTable;
        this.comparisonKeys = comparisonKeys;
        this.converter = converter;
        this.columnLength = Math.min(this.getOldColumnLength(), this.getNewColumnLength());
        this.keyColumns = this.comparisonKeys.getColumns(this.getOldTableMetaData().getTableName());
    }

    public int getNewColumnLength() {
        return this.getColumnLength(this.newTable);
    }

    public int getOldColumnLength() {
        return this.getColumnLength(this.oldTable);
    }

    public ComparableTable getOldTable() {
        return this.oldTable;
    }

    public ComparableTable getNewTable() {
        return this.newTable;
    }

    public int getColumnLength() {
        return this.columnLength;
    }

    public List<String> getKeyColumns() {
        return this.keyColumns;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.comparisonKeys;
    }

    public IDataSetConverter getConverter() {
        return this.converter;
    }

    public String getTableName() {
        return this.getOldTableMetaData().getTableName();
    }

    public Column getOldColumn(final int i) {
        return this.getColumn(i, this.getOldTableMetaData());
    }

    public Column getNewColumn(final int i) {
        return this.getColumn(i, this.getNewTableMetaData());
    }

    public ITableMetaData getOldTableMetaData() {
        return this.getTableMetaData(this.oldTable);
    }

    public ITableMetaData getNewTableMetaData() {
        return this.getTableMetaData(this.newTable);
    }

    protected ITableMetaData getTableMetaData(final ComparableTable table) {
        return table.getTableMetaData();
    }

    protected int getColumnLength(final ComparableTable table) {
        try {
            return this.getTableMetaData(table).getColumns().length;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected Column getColumn(final int i, final ITableMetaData tableMetaData) {
        try {
            return tableMetaData.getColumns()[i];
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}

