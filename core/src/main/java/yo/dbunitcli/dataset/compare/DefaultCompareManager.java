package yo.dbunitcli.dataset.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultCompareManager implements DataSetCompare.Manager {

    private static final String COLUMN_NAME_ROW_INDEX = "$ROW_INDEX";
    private static final Column COLUMN_ROW_INDEX = new Column(COLUMN_NAME_ROW_INDEX, DataType.NUMERIC);

    @Override
    public CompareResult toCompareResult(ComparableDataSet oldDataSet, ComparableDataSet newDataSet, List<CompareDiff> results) {
        return new TableCompareResult(oldDataSet.getSrc(), newDataSet.getSrc(), results);
    }

    @Override
    public List<CompareDiff> compareTable(DataSetCompare.TableCompare tableCompare) {
        List<CompareDiff> results = new ArrayList<>();
        getTableCompareStrategies().forEach(it -> results.addAll(it.apply(tableCompare)));
        return results;
    }

    protected Stream<Function<DataSetCompare.TableCompare, List<CompareDiff>>> getTableCompareStrategies() {
        return Stream.of(this.compareColumnCount()
                , this.searchModifyAndDeleteColumns()
                , this.searchAddColumns()
                , this.rowCount()
                , this.compareRow()
        );
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> compareColumnCount() {
        return it -> {
            try {
                List<CompareDiff> results = new ArrayList<>();
                ITableMetaData oldMetaData = it.getOldTable().getTableMetaData();
                ITableMetaData newMetaData = it.getNewTable().getTableMetaData();
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
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> searchModifyAndDeleteColumns() {
        return it -> {
            try {
                List<CompareDiff> results = new ArrayList<>();
                ITableMetaData oldMetaData = it.getOldTable().getTableMetaData();
                ITableMetaData newMetaData = it.getNewTable().getTableMetaData();
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
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> searchAddColumns() {
        return it -> {
            try {
                List<CompareDiff> results = new ArrayList<>();
                ITableMetaData oldMetaData = it.getOldTable().getTableMetaData();
                ITableMetaData newMetaData = it.getNewTable().getTableMetaData();
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
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> rowCount() {
        return it -> {
            List<CompareDiff> results = new ArrayList<>();
            final int newRows = it.getNewTable().getRowCount();
            final int oldRows = it.getOldTable().getRowCount();
            if (oldRows != newRows) {
                results.add(CompareDiff.Type.ROWS_COUNT.of()
                        .setTargetName(it.getOldTable().getTableMetaData().getTableName())
                        .setRows(Math.abs(oldRows - newRows))
                        .setOldDefine(String.valueOf(oldRows))
                        .setNewDefine(String.valueOf(newRows))
                        .build());
            }
            return results;
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> compareRow() {
        return it -> {
            try {
                return new RowCompare(it).exec();
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    public static class RowCompare {
        protected final ComparableTable oldTable;
        protected final ComparableTable newTable;
        protected final AddSettingColumns comparisonKeys;
        protected final IDataSetConverter writer;
        protected List<String> keyColumns;
        protected final int columnLength;
        protected Map<Integer, CompareDiff> modifyValues;
        protected DiffTable modifyDiffTable;
        protected DefaultTable deleteDiffTable;
        protected DefaultTable addDiffTable;

        public RowCompare(DataSetCompare.TableCompare it) throws DataSetException {
            this.oldTable = it.getOldTable();
            this.newTable = it.getNewTable();
            this.comparisonKeys = it.getComparisonKeys();
            this.writer = it.getConverter();
            this.columnLength = Math.min(this.newTable.getTableMetaData().getColumns().length, this.oldTable.getTableMetaData().getColumns().length);
            this.keyColumns = this.comparisonKeys.getColumns(this.oldTable.getTableMetaData().getTableName());
            this.modifyValues = new HashMap<>();
        }

        protected List<CompareDiff> exec() throws DataSetException {
            final int oldRows = this.oldTable.getRowCount();
            Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = this.newTable.getRows(this.keyColumns);
            this.modifyDiffTable = DiffTable.from(this.oldTable.getTableMetaData(), this.columnLength);
            this.deleteDiffTable = this.toITable(this.oldTable, "$DELETE");
            this.addDiffTable = this.toITable(this.newTable, "$ADD");
            int deleteRow = 0;
            for (int rowNum = 0; rowNum < oldRows; rowNum++) {
                Object[] oldRow = this.oldTable.getRow(rowNum, this.columnLength);
                CompareKeys key = this.oldTable.getKey(rowNum, this.keyColumns);
                if (newRowLists.containsKey(key)) {
                    Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
                    this.compareKey(oldRow, rowEntry.getValue(), key.newRowNum(rowEntry.getKey()));
                    newRowLists.remove(key);
                } else {
                    Object[] row = this.oldTable.getRow(rowNum);
                    this.deleteDiffTable.addRow(Lists.asList(rowNum, row).toArray(new Object[row.length + 1]));
                    deleteRow++;
                }
            }
            this.countAddDiff(newRowLists);
            List<CompareDiff> results = new ArrayList<>(this.modifyValues.values());
            if (deleteRow > 0) {
                results.add(CompareDiff.Type.KEY_DELETE.of()
                        .setTargetName(this.oldTable.getTableMetaData().getTableName())
                        .setRows(deleteRow)
                        .setOldDefine(String.valueOf(deleteRow))
                        .setNewDefine("0")
                        .build());
            }
            if (newRowLists.size() > 0) {
                results.add(CompareDiff.Type.KEY_ADD.of()
                        .setTargetName(this.newTable.getTableMetaData().getTableName())
                        .setRows(newRowLists.size())
                        .setOldDefine("0")
                        .setNewDefine(String.valueOf(newRowLists.size()))
                        .build());
            }
            if (!this.hasNoTableRows()) {
                this.writer.startDataSet();
                this.writeModifyTable();
                this.writeAddRows();
                this.writeDeleteRows();
                this.writer.endDataSet();
            }
            return results;
        }

        protected boolean hasNoTableRows() {
            return this.modifyDiffTable.getRowCount() == 0 && this.deleteDiffTable.getRowCount() == 0 && this.addDiffTable.getRowCount() == 0;
        }

        protected void countAddDiff(Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists) throws DataSetException {
            for (CompareKeys targetKey : newRowLists.keySet()) {
                Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                this.addDiffTable.addRow(Lists.asList(row.getKey()
                        , row.getValue()).toArray(new Object[row.getValue().length + 1]));
            }
        }

        protected void writeModifyTable() throws DataSetException {
            if (this.modifyDiffTable.getRowCount() > 0) {
                SortedTable sortedTable = new SortedTable(this.modifyDiffTable, this.modifyDiffTable.getTableMetaData().getPrimaryKeys());
                sortedTable.setUseComparable(true);
                this.writer.write(sortedTable);
            }
        }

        protected void compareKey(Object[] oldRow, Object[] newRow, CompareKeys key) throws DataSetException {
            int diff = 0;
            for (int columnIndex = 0, columnLength = oldRow.length; columnIndex < columnLength; columnIndex++) {
                if (!Objects.equals(oldRow[columnIndex], newRow[columnIndex])) {
                    this.addRowToModifyTable(key, columnIndex, diff++ == 0);
                    this.countModify(columnIndex);
                }
            }
        }

        protected void addRowToModifyTable(CompareKeys key, int columnIndex, boolean firstDiff) throws DataSetException {
            if (firstDiff) {
                this.modifyDiffTable.addRow(key, columnIndex
                        , this.oldTable.get(key, this.keyColumns, this.columnLength)
                        , this.newTable.get(key, this.keyColumns, this.columnLength));
            } else {
                this.modifyDiffTable.addDiffColumn(key, this.keyColumns, columnIndex);
            }
        }

        protected void countModify(int columnIndex) throws DataSetException {
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
        }

        protected void writeDeleteRows() throws DataSetException {
            if (this.deleteDiffTable.getRowCount() > 0) {
                SortedTable sortedTable = new SortedTable(this.deleteDiffTable, new Column[]{COLUMN_ROW_INDEX});
                sortedTable.setUseComparable(true);
                this.writer.write(sortedTable);
            }
        }

        protected void writeAddRows() throws DataSetException {
            if (this.addDiffTable.getRowCount() > 0) {
                SortedTable sortedTable = new SortedTable(this.addDiffTable, new Column[]{COLUMN_ROW_INDEX});
                sortedTable.setUseComparable(true);
                this.writer.write(sortedTable);
            }
        }

        protected DefaultTable toITable(ComparableTable oldTable, String aTableName) throws DataSetException {
            ITableMetaData origin = oldTable.getTableMetaData();
            Column[] columns = Lists.asList(COLUMN_ROW_INDEX, origin.getColumns()).toArray(new Column[origin.getColumns().length + 1]);
            DefaultTableMetaData metaData = new DefaultTableMetaData(origin.getTableName() + aTableName, columns, new String[]{COLUMN_NAME_ROW_INDEX});
            return new DefaultTable(metaData);
        }
    }
}
