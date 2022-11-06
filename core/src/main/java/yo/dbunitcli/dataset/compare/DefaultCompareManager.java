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
    public CompareResult toCompareResult(final ComparableDataSet oldDataSet, final ComparableDataSet newDataSet, final List<CompareDiff> results) {
        return new TableCompareResult(oldDataSet.getSrc(), newDataSet.getSrc(), results);
    }

    @Override
    public List<CompareDiff> compareTable(final DataSetCompare.TableCompare tableCompare) {
        final List<CompareDiff> results = new ArrayList<>();
        this.getTableCompareStrategies().forEach(it -> results.addAll(it.apply(tableCompare)));
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
                final List<CompareDiff> results = new ArrayList<>();
                final ITableMetaData oldMetaData = it.getOldTable().getTableMetaData();
                final ITableMetaData newMetaData = it.getNewTable().getTableMetaData();
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
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> searchModifyAndDeleteColumns() {
        return it -> {
            try {
                final List<CompareDiff> results = new ArrayList<>();
                final ITableMetaData oldMetaData = it.getOldTable().getTableMetaData();
                final ITableMetaData newMetaData = it.getNewTable().getTableMetaData();
                final String tableName = oldMetaData.getTableName();
                final int newColumns = newMetaData.getColumns().length;
                final int oldColumns = oldMetaData.getColumns().length;
                IntStream.range(0, oldColumns).forEach(i -> {
                    final Column oldColumn = this.getColumn(oldMetaData, i);
                    if (i < newColumns) {
                        final Column newColumn = this.getColumn(newMetaData, i);
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
                });
                return results;
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> searchAddColumns() {
        return it -> {
            try {
                final List<CompareDiff> results = new ArrayList<>();
                final ITableMetaData oldMetaData = it.getOldTable().getTableMetaData();
                final ITableMetaData newMetaData = it.getNewTable().getTableMetaData();
                final String tableName = oldMetaData.getTableName();
                final int newColumns = newMetaData.getColumns().length;
                final int oldColumns = oldMetaData.getColumns().length;
                if (oldColumns < newColumns) {
                    IntStream.range(oldColumns, newColumns).forEach(i -> {
                        final Column newColumn = this.getColumn(newMetaData, i);
                        results.add(CompareDiff.Type.COLUMNS_ADD.of()
                                .setTargetName(tableName)
                                .setNewDefine(newColumn.getColumnName())
                                .setColumnIndex(i)
                                .build());
                    });
                }
                return results;
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        };
    }

    protected Function<DataSetCompare.TableCompare, List<CompareDiff>> rowCount() {
        return it -> {
            final List<CompareDiff> results = new ArrayList<>();
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
        return it -> new RowCompare(it, this.getRowResultHandler(it)).exec();
    }

    protected RowCompareResultHandler getRowResultHandler(final DataSetCompare.TableCompare it) {
        return new DiffWriteRowCompareResultHandler(it).compose(new DataRowCompareResultHandler(it));
    }

    private Column getColumn(final ITableMetaData oldMetaData, final int columnIndex) {
        try {
            return oldMetaData.getColumns()[columnIndex];
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public static class RowCompare {
        protected final ComparableTable oldTable;
        protected final ComparableTable newTable;
        protected final AddSettingColumns comparisonKeys;
        protected final IDataSetConverter converter;
        protected final int columnLength;
        protected List<String> keyColumns;
        protected RowCompareResultHandler handler;

        protected RowCompare(final DataSetCompare.TableCompare it, final RowCompareResultHandler handler) {
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
            final Map<CompareKeys, Map.Entry<Integer, Object[]>> newRowLists = this.newTable.getRows(this.keyColumns);
            IntStream.range(0, oldRows).forEach(rowNum -> {
                final Object[] oldRow = this.oldTable.getRow(rowNum, this.columnLength);
                final CompareKeys key = this.oldTable.getKey(rowNum, this.keyColumns);
                if (newRowLists.containsKey(key)) {
                    final Map.Entry<Integer, Object[]> rowEntry = newRowLists.get(key);
                    this.handler.handleModify(oldRow, rowEntry.getValue(), key.newRowNum(rowEntry.getKey()));
                    newRowLists.remove(key);
                } else {
                    this.handler.handleDelete(rowNum, this.oldTable.getRow(rowNum));
                }
            });
            newRowLists.keySet().forEach(targetKey -> {
                final Map.Entry<Integer, Object[]> row = newRowLists.get(targetKey);
                this.handler.handleAdd(row.getKey(), row.getValue());
            });
            return this.handler.result();
        }
    }
}
