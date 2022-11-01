package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultCompareManager implements DataSetCompare.Manager {

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
                    IntStream.range(oldColumns, newColumns).forEach(i -> {
                        try {
                            Column newColumn = newMetaData.getColumns()[i];
                            results.add(CompareDiff.Type.COLUMNS_ADD.of()
                                    .setTargetName(tableName)
                                    .setNewDefine(newColumn.getColumnName())
                                    .setColumnIndex(i)
                                    .build());
                        } catch (DataSetException e) {
                            throw new AssertionError(e);
                        }
                    });
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
        return it -> new RowCompare(it, getRowResultHandler(it)).exec();
    }

    protected RowCompareResultHandler getRowResultHandler(DataSetCompare.TableCompare it) {
        return new DiffWriteRowCompareResultHandler(it).compose(new DataRowCompareResultHandler(it));
    }

    public static class RowCompare {
        protected final ComparableTable oldTable;
        protected final ComparableTable newTable;
        protected final AddSettingColumns comparisonKeys;
        protected final IDataSetConverter converter;
        protected final int columnLength;
        protected List<String> keyColumns;
        protected RowCompareResultHandler handler;

        protected RowCompare(DataSetCompare.TableCompare it, RowCompareResultHandler handler) {
            this.oldTable = it.getOldTable();
            this.newTable = it.getNewTable();
            this.comparisonKeys = it.getComparisonKeys();
            this.converter = it.getConverter();
            this.columnLength = it.getColumnLength();
            this.keyColumns = it.getKeyColumns();
            this.handler = handler;
        }

        protected List<CompareDiff> exec() {
            final int oldRows = this.oldTable.getRowCount();
            Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = this.newTable.getRows(this.keyColumns);
            IntStream.range(0, oldRows).forEach(rowNum -> {
                Object[] oldRow = this.oldTable.getRow(rowNum, this.columnLength);
                CompareKeys key = this.oldTable.getKey(rowNum, this.keyColumns);
                if (newRowLists.containsKey(key)) {
                    Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
                    this.handler.handleModify(oldRow, rowEntry.getValue(), key.newRowNum(rowEntry.getKey()));
                    newRowLists.remove(key);
                } else {
                    this.handler.handleDelete(rowNum, this.oldTable.getRow(rowNum));
                }
            });
            newRowLists.keySet().forEach(targetKey -> {
                Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                this.handler.handleAdd(row.getKey(), row.getValue());
            });
            return this.handler.result();
        }
    }
}
