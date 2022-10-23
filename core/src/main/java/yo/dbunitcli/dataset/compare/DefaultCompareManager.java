package yo.dbunitcli.dataset.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.*;

import java.util.*;

public class DefaultCompareManager implements Compare.Manager {

    private static final String COLUMN_NAME_ROW_INDEX = "$ROW_INDEX";
    private static final Column COLUMN_ROW_INDEX = new Column(COLUMN_NAME_ROW_INDEX, DataType.NUMERIC);

    @Override
    public CompareResult toCompareResult(ComparableDataSet oldDataSet, ComparableDataSet newDataSet, List<CompareDiff> results) {
        return new TableCompareResult(oldDataSet.getSrc(), newDataSet.getSrc(), results);
    }

    @Override
    public List<CompareDiff> compareTable(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException {
        return new Main(oldTable, newTable, comparisonKeys, writer).getResults();
    }

    public static class Main {
        protected final ComparableTable oldTable;
        protected final ComparableTable newTable;
        protected final AddSettingColumns comparisonKeys;
        protected final IDataSetConverter writer;
        protected List<String> keyColumns;
        protected DiffTable diffDetailTable;
        protected Map<Integer, CompareDiff> modifyValues;
        protected final int columnLength;

        public Main(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException {
            this.oldTable = oldTable;
            this.newTable = newTable;
            this.comparisonKeys = comparisonKeys;
            this.writer = writer;
            this.columnLength = Math.min(this.newTable.getTableMetaData().getColumns().length, this.oldTable.getTableMetaData().getColumns().length);
            this.keyColumns = this.comparisonKeys.getColumns(this.oldTable.getTableMetaData().getTableName());
        }

        public List<CompareDiff> getResults() throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            if (this.comparisonKeys.hasAdditionalSetting(this.oldTable.getTableMetaData().getTableName())) {
                results.addAll(this.compareColumn());
                results.addAll(this.rowCount());
                results.addAll(this.compareRow());
            }
            return results;
        }

        protected List<CompareDiff> compareColumn() throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            results.addAll(this.compareColumnCount());
            results.addAll(this.searchModifyAndDeleteColumns());
            results.addAll(this.searchAddColumns());
            return results;
        }

        protected List<CompareDiff> compareColumnCount() throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            ITableMetaData oldMetaData = this.oldTable.getTableMetaData();
            ITableMetaData newMetaData = this.newTable.getTableMetaData();
            final String tableName = oldMetaData.getTableName();
            final int newColumns = newMetaData.getColumns().length;
            final int oldColumns = oldMetaData.getColumns().length;
            if (oldColumns != newColumns) {
                results.add(CompareDiff.Type.COLUMNS_COUNT.of()
                        .setTargetName(tableName)
                        .setOldDefine(String.valueOf(oldColumns))
                        .setNewDefine(String.valueOf(newColumns))
                        .build());
            }
            return results;
        }

        protected List<CompareDiff> searchModifyAndDeleteColumns() throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            ITableMetaData oldMetaData = this.oldTable.getTableMetaData();
            ITableMetaData newMetaData = this.newTable.getTableMetaData();
            final String tableName = oldMetaData.getTableName();
            final int newColumns = newMetaData.getColumns().length;
            final int oldColumns = oldMetaData.getColumns().length;
            for (int i = 0; i < oldColumns; i++) {
                Column oldColumn = oldMetaData.getColumns()[i];
                if (i < newColumns) {
                    Column newColumn = newMetaData.getColumns()[i];
                    if (!Objects.equals(oldColumn.getColumnName(), newColumn.getColumnName())) {
                        results.add(CompareDiff.Type.COLUMNS_MODIFY.of()
                                .setTargetName(tableName)
                                .setOldDefine(oldColumn.getColumnName())
                                .setNewDefine(newColumn.getColumnName())
                                .setColumnIndex(i)
                                .build());
                    }
                } else {
                    results.add(CompareDiff.Type.COLUMNS_DELETE.of()
                            .setTargetName(tableName)
                            .setOldDefine(oldColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            }
            return results;
        }

        protected List<CompareDiff> searchAddColumns() throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            ITableMetaData oldMetaData = this.oldTable.getTableMetaData();
            ITableMetaData newMetaData = this.newTable.getTableMetaData();
            final String tableName = oldMetaData.getTableName();
            final int newColumns = newMetaData.getColumns().length;
            final int oldColumns = oldMetaData.getColumns().length;
            if (oldColumns < newColumns) {
                for (int i = 0; oldColumns + i < newColumns; i++) {
                    Column newColumn = newMetaData.getColumns()[oldColumns + i];
                    results.add(CompareDiff.Type.COLUMNS_ADD.of()
                            .setTargetName(tableName)
                            .setNewDefine(newColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            }
            return results;
        }

        protected List<CompareDiff> compareRow() throws DataSetException {
            List<CompareDiff> results = new ArrayList<>();
            final int oldRows = this.oldTable.getRowCount();
            Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = this.newTable.getRows(this.keyColumns);
            List<Integer> deleteRows = new ArrayList<>();
            Set<CompareKeys> addRows = new HashSet<>(newRowLists.keySet());
            this.diffDetailTable = DiffTable.from(this.oldTable.getTableMetaData(), this.columnLength);
            this.modifyValues = new HashMap<>();
            for (int rowNum = 0; rowNum < oldRows; rowNum++) {
                Object[] oldRow = this.oldTable.getRow(rowNum, this.columnLength);
                CompareKeys key = this.oldTable.getKey(rowNum, this.keyColumns);
                if (newRowLists.containsKey(key)) {
                    addRows.remove(key);
                    this.compareKey(newRowLists, oldRow, key);
                } else {
                    deleteRows.add(rowNum);
                }
            }
            if (this.modifyValues.size() == 0 && deleteRows.size() == 0 && addRows.size() == 0) {
                return results;
            }
            this.writer.startDataSet();
            if (this.diffDetailTable.getRowCount() > 0) {
                SortedTable sortedTable = new SortedTable(this.diffDetailTable, this.diffDetailTable.getTableMetaData().getPrimaryKeys());
                sortedTable.setUseComparable(true);
                this.writer.write(sortedTable);
            }
            results.addAll(this.modifyValues.values());
            results.addAll(this.writeDeleteRows(deleteRows));
            results.addAll(this.writeAddRows(newRowLists, addRows));
            this.writer.endDataSet();
            return results;
        }

        protected void compareKey(Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Object[] oldRow, CompareKeys key) throws DataSetException {
            Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
            key = key.newRowNum(rowEntry.getKey());
            Object[] newRow = rowEntry.getValue();
            boolean existsDiff = false;
            for (int i = 0, j = oldRow.length; i < j; i++) {
                if (!this.compareValue(oldRow[i], newRow[i])) {
                    if (!existsDiff) {
                        this.diffDetailTable.addRow(key, i
                                , this.oldTable.get(key, this.keyColumns, this.columnLength)
                                , this.newTable.get(key, this.keyColumns, this.columnLength));
                        existsDiff = true;
                    } else {
                        this.diffDetailTable.addDiffColumn(key, this.keyColumns, i);
                    }
                    if (!this.modifyValues.containsKey(i)) {
                        this.modifyValues.put(i, CompareDiff.Type.MODIFY_VALUE.of()
                                .setTargetName(this.oldTable.getTableMetaData().getTableName())
                                .setOldDefine(this.oldTable.getTableMetaData().getColumns()[i].getColumnName())
                                .setNewDefine(this.newTable.getTableMetaData().getColumns()[i].getColumnName())
                                .setColumnIndex(i)
                                .setRows(1)
                                .build());
                    } else {
                        this.modifyValues.computeIfPresent(i, (k, v) -> v.edit(builder -> builder.setRows(builder.getRows() + 1)));
                    }
                }
            }
        }

        protected boolean compareValue(Object oldVal, Object newVal) {
            return Objects.equals(oldVal, newVal);
        }

        protected List<CompareDiff> writeDeleteRows(List<Integer> deleteRows) throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            if (deleteRows.size() > 0) {
                DefaultTable diffDetailTable = this.toITable(this.oldTable, "$DELETE");
                for (int rowNum : deleteRows) {
                    Object[] row = this.oldTable.getRow(rowNum);
                    row = Lists.asList(rowNum, row).toArray(new Object[row.length + 1]);
                    diffDetailTable.addRow(row);
                }
                SortedTable sortedTable = new SortedTable(diffDetailTable, new Column[]{COLUMN_ROW_INDEX});
                sortedTable.setUseComparable(true);
                this.writer.write(sortedTable);
                results.add(CompareDiff.Type.KEY_DELETE.of()
                        .setTargetName(this.oldTable.getTableMetaData().getTableName())
                        .setRows(deleteRows.size())
                        .setOldDefine(String.valueOf(deleteRows.size()))
                        .setNewDefine("0")
                        .build());
            }
            return results;
        }

        protected List<CompareDiff> writeAddRows(Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists, Set<CompareKeys> addRows) throws DataSetException {
            List<CompareDiff> results = Lists.newArrayList();
            if (addRows.size() > 0) {
                DefaultTable diffDetailTable = this.toITable(this.newTable, "$ADD");
                for (CompareKeys targetKey : addRows) {
                    Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                    Object[] convertRow = Lists.asList(row.getKey(), row.getValue()).toArray(new Object[row.getValue().length + 1]);
                    diffDetailTable.addRow(convertRow);
                }
                SortedTable sortedTable = new SortedTable(diffDetailTable, new Column[]{COLUMN_ROW_INDEX});
                sortedTable.setUseComparable(true);
                this.writer.write(sortedTable);
                results.add(CompareDiff.Type.KEY_ADD.of()
                        .setTargetName(this.newTable.getTableMetaData().getTableName())
                        .setRows(addRows.size())
                        .setOldDefine("0")
                        .setNewDefine(String.valueOf(addRows.size()))
                        .build());
            }
            return results;
        }

        protected List<CompareDiff> rowCount() {
            List<CompareDiff> results = Lists.newArrayList();
            final int newRows = this.newTable.getRowCount();
            final int oldRows = this.oldTable.getRowCount();
            if (oldRows != newRows) {
                results.add(CompareDiff.Type.ROWS_COUNT.of()
                        .setTargetName(oldTable.getTableMetaData().getTableName())
                        .setRows(Math.abs(oldRows - newRows))
                        .setOldDefine(String.valueOf(oldRows))
                        .setNewDefine(String.valueOf(newRows))
                        .build());
            }
            return results;
        }

        protected DefaultTable toITable(ComparableTable oldTable, String aTableName) throws DataSetException {
            ITableMetaData origin = oldTable.getTableMetaData();
            Column[] columns = Lists.asList(COLUMN_ROW_INDEX, origin.getColumns()).toArray(new Column[origin.getColumns().length + 1]);
            DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{COLUMN_NAME_ROW_INDEX});
            return new DefaultTable(metaData);
        }
    }
}
