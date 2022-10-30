package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.*;

public class DataRowCompareResultHandler implements RowCompareResultHandler {
    protected final ComparableTable oldTable;
    protected final ComparableTable newTable;
    private Map<Integer, CompareDiff> modifyValues;
    private int deleteRow = 0;
    private int addRow = 0;

    public DataRowCompareResultHandler(DataSetCompare.TableCompare tableCompare) {
        this.oldTable = tableCompare.getOldTable();
        this.newTable = tableCompare.getNewTable();
        this.modifyValues = new HashMap<>();
    }

    @Override
    public void handleModify(Object[] oldRow, Object[] newRow, CompareKeys key) {
        for (int columnIndex = 0, columnLength = oldRow.length; columnIndex < columnLength; columnIndex++) {
            if (!Objects.equals(oldRow[columnIndex], newRow[columnIndex])) {
                addModify(columnIndex);
            }
        }
    }

    @Override
    public void handleDelete(int rowNum, Object[] row) {
        this.deleteRow++;
    }

    @Override
    public void handleAdd(int rowNum, Object[] row) {
        this.addRow++;
    }

    @Override
    public List<CompareDiff> result() {
        List<CompareDiff> results = this.modifyDiff();
        results.addAll(this.deleteDiff());
        results.addAll(this.addDiff());
        return results;
    }

    protected void addModify(int columnIndex) {
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
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected ArrayList<CompareDiff> modifyDiff() {
        return new ArrayList<>(this.modifyValues.values());
    }

    protected List<CompareDiff> deleteDiff() {
        List<CompareDiff> results = new ArrayList<>();
        if (this.deleteRow > 0) {
            results.add(CompareDiff.Type.KEY_DELETE.of()
                    .setTargetName(this.oldTable.getTableMetaData().getTableName())
                    .setRows(deleteRow)
                    .setOldDefine(String.valueOf(deleteRow))
                    .setNewDefine("0")
                    .build());
        }
        return results;
    }

    protected List<CompareDiff> addDiff() {
        List<CompareDiff> results = new ArrayList<>();
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
