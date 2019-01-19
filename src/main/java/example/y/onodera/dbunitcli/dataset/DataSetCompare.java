package example.y.onodera.dbunitcli.dataset;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;

import static example.y.onodera.dbunitcli.dataset.CompareDiff.getBuilder;

public class DataSetCompare implements Compare {

    private ComparableDataSet oldDataSet;

    private ComparableDataSet newDataSet;

    private Map<String, List<String>> comparisonKeys;

    public DataSetCompare(DataSetCompareBuilder builder) {
        this.oldDataSet = builder.getOldDataSet();
        this.newDataSet = builder.getNewDataSet();
        this.comparisonKeys = builder.getComparisonKeys();
    }

    @Override
    public CompareResult exec() throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        results.addAll(this.compareTableCount());
        results.addAll(this.compareTables());
        return new CompareResult(oldDataSet.getSrc(), newDataSet.getSrc(), results);
    }

    protected Collection<? extends CompareDiff> compareTables() throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        Set<String> oldTables = Sets.newHashSet(this.oldDataSet.getTableNames());
        Set<String> newTables = Sets.newHashSet(this.newDataSet.getTableNames());
        results.addAll(this.searchDeleteTables(oldTables, newTables));
        results.addAll(this.searchAddTables(oldTables, newTables));
        results.addAll(searchModifyTables(oldTables, newTables));
        return results;
    }

    protected Collection<? extends CompareDiff> compareTableCount() throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        final int oldTableCounts = this.oldDataSet.getTableNames().length;
        final int newTableCounts = this.newDataSet.getTableNames().length;
        if (oldTableCounts != newTableCounts) {
            results.add(getBuilder(CompareDiff.Type.TABLE_COUNT)
                    .setOldDef(String.valueOf(oldTableCounts))
                    .setNewDef(String.valueOf(newTableCounts))
                    .build());
        }
        return results;
    }

    protected Collection<? extends CompareDiff> searchAddTables(Set<String> oldTables, Set<String> newTables) {
        Set<String> addTables = Sets.filter(newTables, Predicates.not(Predicates.in(oldTables)));
        List<CompareDiff> result = Lists.newArrayList();
        addTables.stream()
                .forEach(name -> result.add(getBuilder(CompareDiff.Type.TABLE_ADD)
                        .setTargetName(name)
                        .setNewDef(name)
                        .build()));
        return result;
    }

    protected Collection<? extends CompareDiff> searchDeleteTables(Set<String> oldTables, Set<String> newTables) {
        Set<String> deleteTables = Sets.filter(oldTables, Predicates.not(Predicates.in(newTables)));
        List<CompareDiff> result = Lists.newArrayList();
        deleteTables.stream()
                .forEach(name -> result.add(getBuilder(CompareDiff.Type.TABLE_DELETE)
                        .setTargetName(name)
                        .setOldDef(name)
                        .build()));
        return result;
    }

    protected List<CompareDiff> searchModifyTables(Set<String> oldTables, Set<String> newTables) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        for (String tableName : Sets.intersection(oldTables, newTables)) {
            ComparableTable oldTable = this.oldDataSet.getTable(tableName);
            ComparableTable newTable = this.newDataSet.getTable(tableName);
            result.addAll(this.compareTable(oldTable,newTable, comparisonKeys));
        }
        return result;
    }

    protected Collection<? extends CompareDiff> compareTable(ComparableTable oldTable, ComparableTable newTable, Map<String,List<String>> comparisonKeys) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        ITableMetaData oldMetaData = oldTable.getTableMetaData();
        ITableMetaData newMetaData = newTable.getTableMetaData();
        result.addAll(this.compareColumnDef(oldMetaData, newMetaData));
        List<String> key = comparisonKeys
                .entrySet()
                .stream()
                .filter(it -> oldMetaData.getTableName().contains(it.getKey()))
                .findAny()
                .get()
                .getValue();
        result.addAll(this.compareRow(oldTable, newTable, key));
        return result;
    }

    protected Collection<? extends CompareDiff> compareColumnDef(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        result.addAll(this.compareColumnCount(oldMetaData, newMetaData));
        result.addAll(this.searchModifyAndDeleteColumns(oldMetaData, newMetaData));
        result.addAll(this.searchAddColumns(oldMetaData, newMetaData));
        return result;
    }

    protected Collection<? extends CompareDiff> compareColumnCount(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        List<CompareDiff> results = Lists.newArrayList();
        if (oldColumns != newColumns) {
            results.add(getBuilder(CompareDiff.Type.COLUMNS_COUNT)
                    .setTargetName(tableName)
                    .setOldDef(String.valueOf(oldColumns))
                    .setNewDef(String.valueOf(newColumns))
                    .build());
        }
        return results;
    }

    protected Collection<? extends CompareDiff> searchModifyAndDeleteColumns(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        for (int i = 0; i < oldColumns; i++) {
            Column oldColumn = oldMetaData.getColumns()[i];
            if (i < newColumns) {
                Column newColumn = newMetaData.getColumns()[i];
                if (!Objects.equals(oldColumn.getColumnName(), newColumn.getColumnName())) {
                    result.add(getBuilder(CompareDiff.Type.COLUMNS_MODIFY)
                            .setTargetName(tableName)
                            .setOldDef(oldColumn.getColumnName())
                            .setNewDef(newColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            } else {
                result.add(getBuilder(CompareDiff.Type.COLUMNS_DELETE)
                        .setTargetName(tableName)
                        .setOldDef(oldColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
        return result;
    }

    protected Collection<? extends CompareDiff> searchAddColumns(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        if (oldColumns < newColumns) {
            for (int i = 0; oldColumns + i < newColumns; i++) {
                Column newColumn = newMetaData.getColumns()[oldColumns + i];
                result.add(getBuilder(CompareDiff.Type.COLUMNS_ADD)
                        .setTargetName(tableName)
                        .setNewDef(newColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
        return result;
    }

    protected Collection<? extends CompareDiff> compareRow(ComparableTable oldTable, ComparableTable newTable, List<String> keys) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        result.addAll(this.rowCount(oldTable, newTable));
        final int oldRows = oldTable.getRowCount();
        final int columnLength = Math.min(newTable.getTableMetaData().getColumns().length, oldTable.getTableMetaData().getColumns().length);
        Map<CompareKeys, Object[]> newRowLists = newTable.getRows(columnLength, keys);
        Map<Integer, Integer> modifyValues = Maps.newHashMap();
        int deleteRows = 0;
        Set<CompareKeys> addRows = Sets.newHashSet(newRowLists.keySet());
        for (int rowNum = 0; rowNum < oldRows; rowNum++) {
            Object[] oldRow = oldTable.getRow(rowNum, columnLength);
            CompareKeys key = oldTable.getKey(rowNum, keys);
            if (newRowLists.containsKey(key)) {
                addRows.remove(key);
                Object[] newRow = newRowLists.get(key);
                for (int i = 0, j = oldRow.length; i < j; i++) {
                    if (!Objects.equals(oldRow[i], newRow[i])) {
                        modifyValues.merge(i, 1, (oldVal, initVal) -> oldVal + initVal);
                    }
                }
            } else {
                deleteRows++;
            }
        }
        if (modifyValues.size() > 0) {
            for (Map.Entry<Integer, Integer> entry : modifyValues.entrySet()) {
                result.add(getBuilder(CompareDiff.Type.MODIFY_VALUE)
                        .setTargetName(oldTable.getTableMetaData().getTableName())
                        .setOldDef(oldTable.getTableMetaData().getColumns()[entry.getKey()].getColumnName())
                        .setNewDef(newTable.getTableMetaData().getColumns()[entry.getKey()].getColumnName())
                        .setColumnIndex(entry.getKey())
                        .setRows(entry.getValue())
                        .build());
            }
        }
        if (deleteRows > 0) {
            result.add(getBuilder(CompareDiff.Type.KEY_DELETE)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(deleteRows)
                    .setOldDef(String.valueOf(deleteRows))
                    .setNewDef("0")
                    .build());
        }
        if (addRows.size() > 0) {
            result.add(getBuilder(CompareDiff.Type.KEY_ADD)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(addRows.size())
                    .setOldDef("0")
                    .setNewDef(String.valueOf(deleteRows))
                    .build());
        }
        return result;
    }

    protected Collection<? extends CompareDiff> rowCount(ComparableTable oldTable, ComparableTable newTable) {
        List<CompareDiff> result = Lists.newArrayList();
        final int newRows = newTable.getRowCount();
        final int oldRows = oldTable.getRowCount();
        if (oldRows != newRows) {
            result.add(getBuilder(CompareDiff.Type.ROWS_COUNT)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(Math.abs(oldRows - newRows))
                    .setOldDef(String.valueOf(oldRows))
                    .setNewDef(String.valueOf(newRows))
                    .build());
        }
        return result;
    }

}
