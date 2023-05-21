package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DiffWriteRowCompareResultHandler implements RowCompareResultHandler {
    private static final String COLUMN_NAME_ROW_INDEX = "$ROW_INDEX";
    private static final Column COLUMN_ROW_INDEX = new Column(COLUMN_NAME_ROW_INDEX, DataType.NUMERIC);
    private final TableCompare tableCompare;
    private final DiffTable modifyDiffTable;
    private final DefaultTable deleteDiffTable;
    private final DefaultTable addDiffTable;

    protected DiffWriteRowCompareResultHandler(final TableCompare it) {
        this.tableCompare = it;
        this.modifyDiffTable = DiffTable.from(this.tableCompare.getOldTable().getTableMetaData(), this.tableCompare.getColumnLength());
        this.deleteDiffTable = this.toITable(this.tableCompare.getOldTable(), "$DELETE");
        this.addDiffTable = this.toITable(this.tableCompare.getNewTable(), "$ADD");
    }

    @Override
    public void handleModify(final Object[] oldRow, final Object[] newRow, final CompareKeys key) {
        final List<Integer> targetRows = new ArrayList<>();
        IntStream.range(0, oldRow.length).forEach(columnIndex -> {
            if (!Objects.equals(oldRow[columnIndex], newRow[columnIndex])) {
                if (targetRows.size() == 0) {
                    targetRows.addAll(this.modifyDiffTable.addRow(key, columnIndex, oldRow, newRow));
                } else {
                    this.modifyDiffTable.addDiffColumn(targetRows, columnIndex);
                }
            }
        });
    }

    @Override
    public void handleDelete(final int rowNum, final Object[] row) {
        this.addRow(rowNum, this.deleteDiffTable, row);
    }

    @Override
    public void handleAdd(final int rowNum, final Object[] row) {
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
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        }
        return new ArrayList<>();
    }

    protected void addRow(final int rowNum, final DefaultTable addDiffTable, final Object... row) {
        try {
            addDiffTable.addRow(Stream.concat(Stream.of(rowNum), Stream.of(row)).toArray(Object[]::new));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected boolean needDetail() {
        return this.modifyDiffTable.getRowCount() > 0 || this.deleteDiffTable.getRowCount() > 0 || this.addDiffTable.getRowCount() > 0;
    }

    protected void writeModifyRowsTable() {
        try {
            this.writeTable(this.modifyDiffTable, this.modifyDiffTable.getTableMetaData().getPrimaryKeys());
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void writeDeleteRowsTable() {
        this.writeTable(this.deleteDiffTable, new Column[]{COLUMN_ROW_INDEX});
    }

    protected void writeAddRowsTable() {
        this.writeTable(this.addDiffTable, new Column[]{COLUMN_ROW_INDEX});
    }

    protected void writeTable(final DefaultTable diffTable, final Column[] columns) {
        try {
            if (diffTable.getRowCount() > 0) {
                final SortedTable sortedTable = new SortedTable(diffTable, columns);
                sortedTable.setUseComparable(true);
                this.tableCompare.getConverter().convert(sortedTable);
            }
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected DefaultTable toITable(final ComparableTable oldTable, final String aTableName) {
        try {
            final ITableMetaData origin = oldTable.getTableMetaData();
            final Column[] columns = Stream.concat(Stream.of(COLUMN_ROW_INDEX), Stream.of(origin.getColumns()))
                    .toArray(Column[]::new);
            final DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{COLUMN_NAME_ROW_INDEX});
            return new DefaultTable(metaData);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
