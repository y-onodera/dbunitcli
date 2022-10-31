package yo.dbunitcli.dataset.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiffWriteRowCompareResultHandler implements RowCompareResultHandler {
    private static final String COLUMN_NAME_ROW_INDEX = "$ROW_INDEX";
    private static final Column COLUMN_ROW_INDEX = new Column(COLUMN_NAME_ROW_INDEX, DataType.NUMERIC);
    private final DataSetCompare.TableCompare tableCompare;
    private final DiffTable modifyDiffTable;
    private final DefaultTable deleteDiffTable;
    private final DefaultTable addDiffTable;

    protected DiffWriteRowCompareResultHandler(DataSetCompare.TableCompare it) {
        this.tableCompare = it;
        this.modifyDiffTable = DiffTable.from(this.tableCompare.getOldTable().getTableMetaData(), this.tableCompare.getColumnLength());
        this.deleteDiffTable = this.toITable(this.tableCompare.getOldTable(), "$DELETE");
        this.addDiffTable = this.toITable(this.tableCompare.getNewTable(), "$ADD");
    }

    @Override
    public void handleModify(Object[] oldRow, Object[] newRow, CompareKeys key) {
        int diff = 0;
        for (int columnIndex = 0, columnLength = oldRow.length; columnIndex < columnLength; columnIndex++) {
            if (!Objects.equals(oldRow[columnIndex], newRow[columnIndex])) {
                if (diff++ == 0) {
                    this.modifyDiffTable.addRow(key, columnIndex
                            , this.tableCompare.getOldTable().get(key, this.tableCompare.getKeyColumns(), this.tableCompare.getColumnLength())
                            , this.tableCompare.getNewTable().get(key, this.tableCompare.getKeyColumns(), this.tableCompare.getColumnLength()));
                } else {
                    this.modifyDiffTable.addDiffColumn(key, this.tableCompare.getKeyColumns(), columnIndex);
                }
            }
        }
    }

    @Override
    public void handleDelete(int rowNum, Object[] row) {
        this.addRow(rowNum, this.deleteDiffTable, row);
    }

    @Override
    public void handleAdd(int rowNum, Object[] row) {
        this.addRow(rowNum, this.addDiffTable, row);
    }

    @Override
    public List<CompareDiff> result() {
        if (this.needDetail()) {
            try {
                this.tableCompare.getConverter().startDataSet();
                this.writeModifyRowsTable();
                this.writeAddRowsTable();
                this.writeDeleteRowsTable();
                this.tableCompare.getConverter().endDataSet();
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        }
        return new ArrayList<>();
    }

    protected void addRow(int rowNum, DefaultTable addDiffTable, Object... row) {
        try {
            addDiffTable.addRow(Lists.asList(rowNum, row).toArray(new Object[row.length + 1]));
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected boolean needDetail() {
        return this.modifyDiffTable.getRowCount() > 0 || this.deleteDiffTable.getRowCount() > 0 || this.addDiffTable.getRowCount() > 0;
    }

    protected void writeModifyRowsTable() {
        try {
            writeTable(this.modifyDiffTable, this.modifyDiffTable.getTableMetaData().getPrimaryKeys());
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void writeDeleteRowsTable() {
        this.writeTable(this.deleteDiffTable, new Column[]{COLUMN_ROW_INDEX});
    }

    protected void writeAddRowsTable() {
        this.writeTable(this.addDiffTable, new Column[]{COLUMN_ROW_INDEX});
    }

    protected void writeTable(DefaultTable diffTable, Column[] columns) {
        try {
            if (diffTable.getRowCount() > 0) {
                SortedTable sortedTable = new SortedTable(diffTable, columns);
                sortedTable.setUseComparable(true);
                this.tableCompare.getConverter().write(sortedTable);
            }
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected DefaultTable toITable(ComparableTable oldTable, String aTableName) {
        try {
            ITableMetaData origin = oldTable.getTableMetaData();
            Column[] columns = Lists.asList(COLUMN_ROW_INDEX, origin.getColumns()).toArray(new Column[origin.getColumns().length + 1]);
            DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{COLUMN_NAME_ROW_INDEX});
            return new DefaultTable(metaData);
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
