package yo.dbunitcli.compare;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;
import yo.dbunitcli.dataset.IDataSetWriter;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;

import java.util.*;

import static yo.dbunitcli.compare.CompareDiff.getBuilder;

public class DataSetCompare implements Compare {

    private ComparableDataSet oldDataSet;

    private ComparableDataSet newDataSet;

    private CompareSetting comparisonKeys;

    private IDataSetWriter writer;

    private List<CompareDiff> results;

    private CompareResult result;

    public DataSetCompare(DataSetCompareBuilder builder) throws DataSetException {
        this.oldDataSet = builder.getOldDataSet();
        this.newDataSet = builder.getNewDataSet();
        this.comparisonKeys = builder.getComparisonKeys();
        this.writer = builder.getDataSetWriter();
        this.results = Lists.newArrayList();
        this.result = this.exec();
    }

    @Override
    public CompareResult result() throws DataSetException {
        return this.result;
    }

    protected CompareResult exec() throws DataSetException {
        this.compareTableCount();
        this.compareTables(writer);
        CompareResult compareResult = new CompareResult(oldDataSet.getSrc(), newDataSet.getSrc(), results);
        writer.write(compareResult.toITable());
        return compareResult;
    }

    protected void compareTableCount() throws DataSetException {
        final int oldTableCounts = this.oldDataSet.getTableNames().length;
        final int newTableCounts = this.newDataSet.getTableNames().length;
        if (oldTableCounts != newTableCounts) {
            this.results.add(getBuilder(CompareDiff.Type.TABLE_COUNT)
                    .setOldDef(String.valueOf(oldTableCounts))
                    .setNewDef(String.valueOf(newTableCounts))
                    .build());
        }
    }

    protected void compareTables(IDataSetWriter writer) throws DataSetException {
        Set<String> oldTables = Sets.newHashSet(this.oldDataSet.getTableNames());
        Set<String> newTables = Sets.newHashSet(this.newDataSet.getTableNames());
        this.searchDeleteTables(oldTables, newTables);
        this.searchAddTables(oldTables, newTables);
        this.searchModifyTables(oldTables, newTables, writer);
    }

    protected void searchAddTables(Set<String> oldTables, Set<String> newTables) {
        Set<String> addTables = Sets.filter(newTables, Predicates.not(Predicates.in(oldTables)));
        addTables.stream()
                .forEach(name -> results.add(getBuilder(CompareDiff.Type.TABLE_ADD)
                        .setTargetName(name)
                        .setNewDef(name)
                        .build()));
    }

    protected void searchDeleteTables(Set<String> oldTables, Set<String> newTables) {
        Set<String> deleteTables = Sets.filter(oldTables, Predicates.not(Predicates.in(newTables)));
        deleteTables.stream()
                .forEach(name -> results.add(getBuilder(CompareDiff.Type.TABLE_DELETE)
                        .setTargetName(name)
                        .setOldDef(name)
                        .build()));
    }

    protected void searchModifyTables(Set<String> oldTables, Set<String> newTables, IDataSetWriter writer) throws DataSetException {
        for (String tableName : Sets.intersection(oldTables, newTables)) {
            ComparableTable oldTable = this.oldDataSet.getTable(tableName);
            ComparableTable newTable = this.newDataSet.getTable(tableName);
            this.compareTable(oldTable, newTable, writer);
        }
    }

    protected void compareTable(ComparableTable oldTable, ComparableTable newTable, IDataSetWriter writer) throws DataSetException {
        ITableMetaData oldMetaData = oldTable.getTableMetaData();
        ITableMetaData newMetaData = newTable.getTableMetaData();
        this.compareColumnCount(oldMetaData, newMetaData);
        this.searchModifyAndDeleteColumns(oldMetaData, newMetaData);
        this.searchAddColumns(oldMetaData, newMetaData);
        List<String> key = this.comparisonKeys.get(oldMetaData.getTableName());
        this.rowCount(oldTable, newTable);
        this.compareRow(oldTable, newTable, key, writer);
    }

    protected void compareColumnCount(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        if (oldColumns != newColumns) {
            this.results.add(getBuilder(CompareDiff.Type.COLUMNS_COUNT)
                    .setTargetName(tableName)
                    .setOldDef(String.valueOf(oldColumns))
                    .setNewDef(String.valueOf(newColumns))
                    .build());
        }
    }

    protected void searchModifyAndDeleteColumns(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        for (int i = 0; i < oldColumns; i++) {
            Column oldColumn = oldMetaData.getColumns()[i];
            if (i < newColumns) {
                Column newColumn = newMetaData.getColumns()[i];
                if (!Objects.equals(oldColumn.getColumnName(), newColumn.getColumnName())) {
                    this.results.add(getBuilder(CompareDiff.Type.COLUMNS_MODIFY)
                            .setTargetName(tableName)
                            .setOldDef(oldColumn.getColumnName())
                            .setNewDef(newColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            } else {
                this.results.add(getBuilder(CompareDiff.Type.COLUMNS_DELETE)
                        .setTargetName(tableName)
                        .setOldDef(oldColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
    }

    protected void searchAddColumns(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        final String tableName = oldMetaData.getTableName();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        if (oldColumns < newColumns) {
            for (int i = 0; oldColumns + i < newColumns; i++) {
                Column newColumn = newMetaData.getColumns()[oldColumns + i];
                this.results.add(getBuilder(CompareDiff.Type.COLUMNS_ADD)
                        .setTargetName(tableName)
                        .setNewDef(newColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
    }

    protected void compareRow(ComparableTable oldTable, ComparableTable newTable, List<String> keys, IDataSetWriter writer) throws DataSetException {
        final int oldRows = oldTable.getRowCount();
        final int columnLength = Math.min(newTable.getTableMetaData().getColumns().length, oldTable.getTableMetaData().getColumns().length);
        Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = newTable.getRows(keys);
        Map<Integer, List<CompareKeys>> modifyValues = Maps.newHashMap();
        List<Integer> deleteRows = Lists.newArrayList();
        Set<CompareKeys> addRows = Sets.newHashSet(newRowLists.keySet());
        for (int rowNum = 0; rowNum < oldRows; rowNum++) {
            Object[] oldRow = oldTable.getRow(rowNum, columnLength);
            CompareKeys key = oldTable.getKey(rowNum, keys);
            if (newRowLists.containsKey(key)) {
                addRows.remove(key);
                Object[] newRow = newRowLists.get(key).getValue();
                for (int i = 0, j = oldRow.length; i < j; i++) {
                    if (!Objects.equals(oldRow[i], newRow[i])) {
                        if (modifyValues.containsKey(i)) {
                            modifyValues.get(i).add(key);
                        } else {
                            modifyValues.put(i, Lists.newArrayList(key));
                        }
                    }
                }
            } else {
                deleteRows.add(rowNum);
            }
        }
        if (modifyValues.size() > 0) {
            final ITableMetaData origin = oldTable.getTableMetaData();
            final List<Column> originColumns = Lists.newArrayList(origin.getColumns()).subList(0, columnLength);
            Column[] columns = originColumns.toArray(new Column[columnLength]);
            DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + "$MODIFY", columns);
            DefaultTable diffDetailTable = new DefaultTable(metaData);
            for (Map.Entry<Integer, List<CompareKeys>> entry : modifyValues.entrySet()) {
                for (CompareKeys targetKey : entry.getValue()) {
                    diffDetailTable.addRow(oldTable.get(targetKey, keys, columnLength));
                    diffDetailTable.addRow(newTable.get(targetKey, keys, columnLength));
                }
                this.results.add(getBuilder(CompareDiff.Type.MODIFY_VALUE)
                        .setTargetName(oldTable.getTableMetaData().getTableName())
                        .setOldDef(oldTable.getTableMetaData().getColumns()[entry.getKey()].getColumnName())
                        .setNewDef(newTable.getTableMetaData().getColumns()[entry.getKey()].getColumnName())
                        .setColumnIndex(entry.getKey())
                        .setRows(entry.getValue().size())
                        .build());
            }
            writer.write(diffDetailTable);
        }
        if (deleteRows.size() > 0) {
            DefaultTable diffDetailTable = toDiffTable(oldTable, "$DELETE");
            for (int rowNum : deleteRows) {
                Object[] row = oldTable.getRow(rowNum);
                row = Lists.asList(Integer.valueOf(rowNum), row).toArray(new Object[row.length + 1]);
                diffDetailTable.addRow(row);
            }
            writer.write(new SortedTable(diffDetailTable));
            this.results.add(getBuilder(CompareDiff.Type.KEY_DELETE)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(deleteRows.size())
                    .setOldDef(String.valueOf(deleteRows.size()))
                    .setNewDef("0")
                    .setDetailRows(diffDetailTable)
                    .build());
        }
        if (addRows.size() > 0) {
            DefaultTable diffDetailTable = toDiffTable(newTable, "$ADD");
            for (CompareKeys targetKey : addRows) {
                Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                Object[] convertRow = Lists.asList(Integer.valueOf(row.getKey()), row.getValue()).toArray(new Object[row.getValue().length + 1]);
                diffDetailTable.addRow(convertRow);
            }
            writer.write(new SortedTable(diffDetailTable));
            this.results.add(getBuilder(CompareDiff.Type.KEY_ADD)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(addRows.size())
                    .setOldDef("0")
                    .setNewDef(String.valueOf(addRows.size()))
                    .setDetailRows(diffDetailTable)
                    .build());
        }
    }

    protected void rowCount(ComparableTable oldTable, ComparableTable newTable) {
        final int newRows = newTable.getRowCount();
        final int oldRows = oldTable.getRowCount();
        if (oldRows != newRows) {
            this.results.add(getBuilder(CompareDiff.Type.ROWS_COUNT)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(Math.abs(oldRows - newRows))
                    .setOldDef(String.valueOf(oldRows))
                    .setNewDef(String.valueOf(newRows))
                    .build());
        }
    }

    private DefaultTable toDiffTable(ComparableTable oldTable, String aTableName) throws DataSetException {
        ITableMetaData origin = oldTable.getTableMetaData();
        Column[] columns = Lists.asList(new Column("$ROW_INDEX", DataType.NUMERIC), origin.getColumns()).toArray(new Column[origin.getColumns().length + 1]);
        DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{"$ROW_INDEX"});
        return new DefaultTable(metaData);
    }
}
