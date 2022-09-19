package yo.dbunitcli.dataset.compare;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;
import yo.dbunitcli.dataset.IDataSetConsumer;

import java.util.*;

import static yo.dbunitcli.dataset.compare.CompareDiff.getBuilder;

public class TableDataSetCompare {

    private static final String COLUMN_NAME_ROW_INDEX = "$ROW_INDEX";
    private static final Column COLUMN_ROW_INDEX = new Column(COLUMN_NAME_ROW_INDEX, DataType.NUMERIC);

    public List<CompareDiff> getResults(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConsumer writer) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        ITableMetaData oldMetaData = oldTable.getTableMetaData();
        ITableMetaData newMetaData = newTable.getTableMetaData();
        results.addAll(this.compareColumn(oldMetaData, newMetaData));
        results.addAll(this.rowCount(oldTable, newTable));
        if (comparisonKeys.hasAdditionalSetting(oldMetaData.getTableName())) {
            List<String> key = comparisonKeys.getColumns(oldMetaData.getTableName());
            results.addAll(this.compareRow(oldTable, newTable, key, writer));
        }
        return results;

    }

    protected List<CompareDiff> compareColumn(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        results.addAll(this.compareColumnCount(oldMetaData, newMetaData));
        results.addAll(this.searchModifyAndDeleteColumns(oldMetaData, newMetaData));
        results.addAll(this.searchAddColumns(oldMetaData, newMetaData));
        return results;
    }

    protected List<CompareDiff> compareColumnCount(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        if (oldColumns != newColumns) {
            results.add(getBuilder(CompareDiff.Type.COLUMNS_COUNT)
                    .setTargetName(tableName)
                    .setOldDef(String.valueOf(oldColumns))
                    .setNewDef(String.valueOf(newColumns))
                    .build());
        }
        return results;
    }

    protected List<CompareDiff> searchModifyAndDeleteColumns(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        for (int i = 0; i < oldColumns; i++) {
            Column oldColumn = oldMetaData.getColumns()[i];
            if (i < newColumns) {
                Column newColumn = newMetaData.getColumns()[i];
                if (!Objects.equals(oldColumn.getColumnName(), newColumn.getColumnName())) {
                    results.add(getBuilder(CompareDiff.Type.COLUMNS_MODIFY)
                            .setTargetName(tableName)
                            .setOldDef(oldColumn.getColumnName())
                            .setNewDef(newColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            } else {
                results.add(getBuilder(CompareDiff.Type.COLUMNS_DELETE)
                        .setTargetName(tableName)
                        .setOldDef(oldColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
        return results;
    }

    protected List<CompareDiff> searchAddColumns(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        if (oldColumns < newColumns) {
            for (int i = 0; oldColumns + i < newColumns; i++) {
                Column newColumn = newMetaData.getColumns()[oldColumns + i];
                results.add(getBuilder(CompareDiff.Type.COLUMNS_ADD)
                        .setTargetName(tableName)
                        .setNewDef(newColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
        return results;
    }

    protected List<CompareDiff> compareRow(ComparableTable oldTable, ComparableTable newTable, List<String> keyNames, IDataSetConsumer writer) throws DataSetException {
        List<CompareDiff> results = new ArrayList<>();
        final int oldRows = oldTable.getRowCount();
        final int columnLength = Math.min(newTable.getTableMetaData().getColumns().length, oldTable.getTableMetaData().getColumns().length);
        Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = newTable.getRows(keyNames);
        Map<Integer, List<CompareKeys>> modifyValues = Maps.newHashMap();
        List<Integer> deleteRows = new ArrayList<>();
        Set<CompareKeys> addRows = Sets.newHashSet(newRowLists.keySet());
        for (int rowNum = 0; rowNum < oldRows; rowNum++) {
            Object[] oldRow = oldTable.getRow(rowNum, columnLength);
            CompareKeys key = oldTable.getKey(rowNum, keyNames);
            if (newRowLists.containsKey(key)) {
                addRows.remove(key);
                compareKey(newRowLists, modifyValues, oldRow, key);
            } else {
                deleteRows.add(rowNum);
            }
        }
        if (modifyValues.size() == 0 && deleteRows.size() == 0 && addRows.size() == 0) {
            return results;
        }
        writer.open(oldTable.getTableMetaData().getTableName());
        writer.startDataSet();
        results.addAll(this.writeModifyValues(oldTable, newTable, keyNames, writer, columnLength, modifyValues));
        results.addAll(this.writeDeleteRows(oldTable, writer, deleteRows));
        results.addAll(this.writeAddRows(newTable, writer, newRowLists, addRows));
        writer.endDataSet();
        return results;
    }

    protected void compareKey(Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Map<Integer, List<CompareKeys>> modifyValues, Object[] oldRow, CompareKeys key) throws DataSetException {
        Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
        key = key.newRowNum(rowEntry.getKey());
        Object[] newRow = rowEntry.getValue();
        for (int i = 0, j = oldRow.length; i < j; i++) {
            if (!this.compareValue(oldRow[i], newRow[i])) {
                if (modifyValues.containsKey(i)) {
                    modifyValues.get(i).add(key);
                } else {
                    modifyValues.put(i, Lists.newArrayList(key));
                }
            }
        }
    }

    protected boolean compareValue(Object oldVal, Object newVal) throws DataSetException {
        return Objects.equals(oldVal, newVal);
    }

    protected List<CompareDiff> writeModifyValues(ComparableTable oldTable, ComparableTable newTable, List<String> keyNames, IDataSetConsumer writer, int columnLength, Map<Integer, List<CompareKeys>> modifyValues) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        if (modifyValues.size() > 0) {
            final ITableMetaData origin = oldTable.getTableMetaData();
            DiffTable diffDetailTable = DiffTable.from(origin, columnLength);
            List<CompareKeys> writeRows = Lists.newArrayList();
            for (Map.Entry<Integer, List<CompareKeys>> modifyColumns : modifyValues.entrySet()) {
                for (CompareKeys compareKey : modifyColumns.getValue()) {
                    if (!writeRows.contains(compareKey)) {
                        diffDetailTable.addRow(compareKey, modifyColumns.getKey(), oldTable.get(compareKey, keyNames, columnLength)
                                , newTable.get(compareKey, keyNames, columnLength));
                        writeRows.add(compareKey);
                    } else {
                        diffDetailTable.addDiffColumn(compareKey, keyNames, modifyColumns.getKey());
                    }
                }
                results.add(getBuilder(CompareDiff.Type.MODIFY_VALUE)
                        .setTargetName(oldTable.getTableMetaData().getTableName())
                        .setOldDef(oldTable.getTableMetaData().getColumns()[modifyColumns.getKey()].getColumnName())
                        .setNewDef(newTable.getTableMetaData().getColumns()[modifyColumns.getKey()].getColumnName())
                        .setColumnIndex(modifyColumns.getKey())
                        .setRows(modifyColumns.getValue().size())
                        .build());
            }
            SortedTable sortedTable = new SortedTable(diffDetailTable, diffDetailTable.getTableMetaData().getPrimaryKeys());
            sortedTable.setUseComparable(true);
            writer.write(sortedTable);
        }
        return results;
    }

    protected List<CompareDiff> writeDeleteRows(ComparableTable oldTable, IDataSetConsumer writer, List<Integer> deleteRows) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        if (deleteRows.size() > 0) {
            DefaultTable diffDetailTable = toITable(oldTable, "$DELETE");
            for (int rowNum : deleteRows) {
                Object[] row = oldTable.getRow(rowNum);
                row = Lists.asList(rowNum, row).toArray(new Object[row.length + 1]);
                diffDetailTable.addRow(row);
            }
            SortedTable sortedTable = new SortedTable(diffDetailTable, new Column[]{COLUMN_ROW_INDEX});
            sortedTable.setUseComparable(true);
            writer.write(sortedTable);
            results.add(getBuilder(CompareDiff.Type.KEY_DELETE)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(deleteRows.size())
                    .setOldDef(String.valueOf(deleteRows.size()))
                    .setNewDef("0")
                    .setDetailRows(diffDetailTable)
                    .build());
        }
        return results;
    }

    protected List<CompareDiff> writeAddRows(ComparableTable newTable, IDataSetConsumer writer, Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Set<CompareKeys> addRows) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        if (addRows.size() > 0) {
            DefaultTable diffDetailTable = toITable(newTable, "$ADD");
            for (CompareKeys targetKey : addRows) {
                Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                Object[] convertRow = Lists.asList(row.getKey(), row.getValue()).toArray(new Object[row.getValue().length + 1]);
                diffDetailTable.addRow(convertRow);
            }
            SortedTable sortedTable = new SortedTable(diffDetailTable, new Column[]{COLUMN_ROW_INDEX});
            sortedTable.setUseComparable(true);
            writer.write(sortedTable);
            results.add(getBuilder(CompareDiff.Type.KEY_ADD)
                    .setTargetName(newTable.getTableMetaData().getTableName())
                    .setRows(addRows.size())
                    .setOldDef("0")
                    .setNewDef(String.valueOf(addRows.size()))
                    .setDetailRows(diffDetailTable)
                    .build());
        }
        return results;
    }

    protected List<CompareDiff> rowCount(ComparableTable oldTable, ComparableTable newTable) {
        List<CompareDiff> results = Lists.newArrayList();
        final int newRows = newTable.getRowCount();
        final int oldRows = oldTable.getRowCount();
        if (oldRows != newRows) {
            results.add(getBuilder(CompareDiff.Type.ROWS_COUNT)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(Math.abs(oldRows - newRows))
                    .setOldDef(String.valueOf(oldRows))
                    .setNewDef(String.valueOf(newRows))
                    .build());
        }
        return results;
    }

    private DefaultTable toITable(ComparableTable oldTable, String aTableName) throws DataSetException {
        ITableMetaData origin = oldTable.getTableMetaData();
        Column[] columns = Lists.asList(COLUMN_ROW_INDEX, origin.getColumns()).toArray(new Column[origin.getColumns().length + 1]);
        DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{COLUMN_NAME_ROW_INDEX});
        return new DefaultTable(metaData);
    }

}
