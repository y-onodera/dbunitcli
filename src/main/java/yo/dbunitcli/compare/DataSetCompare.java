package yo.dbunitcli.compare;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static yo.dbunitcli.compare.CompareDiff.getBuilder;

public class DataSetCompare implements Compare {

    private static final String RESULT_TABLE_NAME = "COMPARE_RESULT";
    private static final String COLUMN_NAME_ROW_INDEX = "$ROW_INDEX";
    private static final Column COLUMN_ROW_INDEX = new Column(COLUMN_NAME_ROW_INDEX, DataType.NUMERIC);

    private ComparableDataSet oldDataSet;

    private ComparableDataSet newDataSet;

    private ColumnSetting comparisonKeys;

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
        this.writer.cleanupDirectory();
        this.compareTableCount();
        this.compareTables(this.writer);
        CompareResult compareResult = new CompareResult(this.oldDataSet.getSrc(), this.newDataSet.getSrc(), this.results);
        final ITable table = compareResult.toITable(RESULT_TABLE_NAME);
        this.writer.open(table.getTableMetaData().getTableName());
        this.writer.write(table);
        this.writer.close();
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
                .forEach(name -> this.results.add(getBuilder(CompareDiff.Type.TABLE_ADD)
                        .setTargetName(name)
                        .setNewDef(name)
                        .build()));
    }

    protected void searchDeleteTables(Set<String> oldTables, Set<String> newTables) {
        Set<String> deleteTables = Sets.filter(oldTables, Predicates.not(Predicates.in(newTables)));
        deleteTables.stream()
                .forEach(name -> this.results.add(getBuilder(CompareDiff.Type.TABLE_DELETE)
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
        this.rowCount(oldTable, newTable);
        if (this.comparisonKeys.includeSetting(oldMetaData.getTableName())) {
            List<String> key = this.comparisonKeys.getColumns(oldMetaData.getTableName());
            this.compareRow(oldTable, newTable, key, writer);
        }
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

    protected void compareRow(ComparableTable oldTable, ComparableTable newTable, List<String> keyNames, IDataSetWriter writer) throws DataSetException {
        final int oldRows = oldTable.getRowCount();
        final int columnLength = Math.min(newTable.getTableMetaData().getColumns().length, oldTable.getTableMetaData().getColumns().length);
        Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = newTable.getRows(keyNames);
        Map<Integer, List<CompareKeys>> modifyValues = Maps.newHashMap();
        List<Integer> deleteRows = Lists.newArrayList();
        Set<CompareKeys> addRows = Sets.newHashSet(newRowLists.keySet());
        for (int rowNum = 0; rowNum < oldRows; rowNum++) {
            Object[] oldRow = oldTable.getRow(rowNum, columnLength);
            CompareKeys key = oldTable.getKey(rowNum, keyNames);
            if (newRowLists.containsKey(key)) {
                addRows.remove(key);
                Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
                key = key.newRowNum(rowEntry.getKey());
                Object[] newRow = rowEntry.getValue();
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
        if (modifyValues.size() == 0 && deleteRows.size() == 0 && addRows.size() == 0) {
            return;
        }
        writer.open(oldTable.getTableMetaData().getTableName());
        this.writeModifyValues(oldTable, newTable, keyNames, writer, columnLength, modifyValues);
        this.writeDeleteRows(oldTable, writer, deleteRows);
        this.writeAddRows(newTable, writer, newRowLists, addRows);
        writer.close();
    }

    protected void writeModifyValues(ComparableTable oldTable, ComparableTable newTable, List<String> keyNames, IDataSetWriter writer, int columnLength, Map<Integer, List<CompareKeys>> modifyValues) throws DataSetException {
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
                this.results.add(getBuilder(CompareDiff.Type.MODIFY_VALUE)
                        .setTargetName(oldTable.getTableMetaData().getTableName())
                        .setOldDef(oldTable.getTableMetaData().getColumns()[modifyColumns.getKey()].getColumnName())
                        .setNewDef(newTable.getTableMetaData().getColumns()[modifyColumns.getKey()].getColumnName())
                        .setColumnIndex(modifyColumns.getKey())
                        .setRows(modifyColumns.getValue().size())
                        .build());
            }
            writer.write(new SortedTable(diffDetailTable, diffDetailTable.getTableMetaData().getPrimaryKeys()));
        }
    }

    protected void writeDeleteRows(ComparableTable oldTable, IDataSetWriter writer, List<Integer> deleteRows) throws DataSetException {
        if (deleteRows.size() > 0) {
            DefaultTable diffDetailTable = toITable(oldTable, "$DELETE");
            for (int rowNum : deleteRows) {
                Object[] row = oldTable.getRow(rowNum);
                row = Lists.asList(rowNum, row).toArray(new Object[row.length + 1]);
                diffDetailTable.addRow(row);
            }
            writer.write(new SortedTable(diffDetailTable, new Column[]{COLUMN_ROW_INDEX}));
            this.results.add(getBuilder(CompareDiff.Type.KEY_DELETE)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(deleteRows.size())
                    .setOldDef(String.valueOf(deleteRows.size()))
                    .setNewDef("0")
                    .setDetailRows(diffDetailTable)
                    .build());
        }
    }

    protected void writeAddRows(ComparableTable newTable, IDataSetWriter writer, Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Set<CompareKeys> addRows) throws DataSetException {
        if (addRows.size() > 0) {
            DefaultTable diffDetailTable = toITable(newTable, "$ADD");
            for (CompareKeys targetKey : addRows) {
                Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                Object[] convertRow = Lists.asList(row.getKey(), row.getValue()).toArray(new Object[row.getValue().length + 1]);
                diffDetailTable.addRow(convertRow);
            }
            writer.write(new SortedTable(diffDetailTable, new Column[]{COLUMN_ROW_INDEX}));
            this.results.add(getBuilder(CompareDiff.Type.KEY_ADD)
                    .setTargetName(newTable.getTableMetaData().getTableName())
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

    private DefaultTable toITable(ComparableTable oldTable, String aTableName) throws DataSetException {
        ITableMetaData origin = oldTable.getTableMetaData();
        Column[] columns = Lists.asList(COLUMN_ROW_INDEX, origin.getColumns()).toArray(new Column[origin.getColumns().length + 1]);
        DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{COLUMN_NAME_ROW_INDEX});
        return new DefaultTable(metaData);
    }
}
