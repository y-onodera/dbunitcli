package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.*;
import java.util.stream.IntStream;

public class DataRowCompareResultHandler implements RowCompareResultHandler {
    protected final ComparableTable oldTable;
    protected final ComparableTable newTable;
    private final Map<Integer, CompareDiff> modifyValues;
    private int deleteRow = 0;
    private int addRow = 0;

    public DataRowCompareResultHandler(final DataSetCompare.TableCompare tableCompare) {
        this.oldTable = tableCompare.getOldTable();
        this.newTable = tableCompare.getNewTable();
        this.modifyValues = new HashMap<>();
    }

    @Override
    public void handleModify(final Object[] oldRow, final Object[] newRow, final CompareKeys key) {
        IntStream.range(0, oldRow.length).forEach(columnIndex -> {
            if (!Objects.equals(oldRow[columnIndex], newRow[columnIndex])) {
                this.addModify(columnIndex);
            }
        });
    }

    @Override
    public void handleDelete(final int rowNum, final Object[] row) {
        this.deleteRow++;
    }

    @Override
    public void handleAdd(final int rowNum, final Object[] row) {
        this.addRow++;
    }

    @Override
    public List<CompareDiff> result() {
        final List<CompareDiff> results = new ArrayList<>();
        results.addAll(this.modifyDiff());
        results.addAll(this.deleteDiff());
        results.addAll(this.addDiff());
        return results;
    }

    protected void addModify(final int columnIndex) {
        try {
            if (!this.modifyValues.containsKey(columnIndex)) {
                this.modifyValues.put(columnIndex, CompareDiff.Type.MODIFY_VALUE.of()
                        .setTargetName(this.oldTable.getTableMetaData().getTableName())
                        .setOldDefine(this.oldTable.getTableMetaData().getColumns()[columnIndex].getColumnName())
                        .setNewDefine(this.newTable.getTableMetaData().getColumns()[columnIndex].getColumnName())
                        .setColumnIndex(columnIndex)
                        .setRows(1)
                        .build());
            } else {
                this.modifyValues.computeIfPresent(columnIndex
                        , (k, v) -> v.edit(builder -> builder.setRows(builder.getRows() + 1)));
            }
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected Collection<CompareDiff> modifyDiff() {
        return this.modifyValues.values();
    }

    protected Collection<CompareDiff> deleteDiff() {
        final List<CompareDiff> results = new ArrayList<>();
        if (this.deleteRow > 0) {
            results.add(CompareDiff.Type.KEY_DELETE.of()
                    .setTargetName(this.oldTable.getTableMetaData().getTableName())
                    .setRows(this.deleteRow)
                    .setOldDefine(String.valueOf(this.deleteRow))
                    .setNewDefine("0")
                    .build());
        }
        return results;
    }

    protected Collection<CompareDiff> addDiff() {
        final List<CompareDiff> results = new ArrayList<>();
        if (this.addRow > 0) {
            results.add(CompareDiff.Type.KEY_ADD.of()
                    .setTargetName(this.newTable.getTableMetaData().getTableName())
                    .setRows(this.addRow)
                    .setOldDefine("0")
                    .setNewDefine(String.valueOf(this.addRow))
                    .build());
        }
        return results;
    }
}
