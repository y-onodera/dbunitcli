package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.Column;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultCompareManager implements DataSetCompare.Manager {

    @Override
    public List<CompareDiff> compareTable(final TableCompare tableCompare) {
        return this.getTableCompareStrategies().map(it -> it.apply(tableCompare))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public CompareResult toCompareResult(final ComparableDataSet oldDataSet, final ComparableDataSet newDataSet, final List<CompareDiff> results) {
        return new TableCompareResult(oldDataSet.src(), newDataSet.src(), results);
    }

    protected Stream<Function<TableCompare, List<CompareDiff>>> getTableCompareStrategies() {
        return Stream.of(this.compareColumnCount()
                , this.searchModifyAndDeleteColumns()
                , this.searchAddColumns()
                , this.rowCount()
                , this.compareRow()
        );
    }

    protected Function<TableCompare, List<CompareDiff>> compareColumnCount() {
        return it -> {
            final List<CompareDiff> results = new ArrayList<>();
            final int newColumns = it.getNewColumnLength();
            final int oldColumns = it.getOldColumnLength();
            if (oldColumns != newColumns) {
                results.add(CompareDiff.Type.COLUMNS_COUNT.of()
                        .setTargetName(it.getTableName())
                        .setOldDefine(String.valueOf(oldColumns))
                        .setNewDefine(String.valueOf(newColumns))
                        .build());
            }
            return results;
        };
    }

    protected Function<TableCompare, List<CompareDiff>> searchModifyAndDeleteColumns() {
        return it -> {
            final List<CompareDiff> results = new ArrayList<>();
            final int newColumns = it.getNewColumnLength();
            final int oldColumns = it.getOldColumnLength();
            IntStream.range(0, oldColumns).forEach(i -> {
                final Column oldColumn = it.getOldColumn(i);
                if (i < newColumns) {
                    final Column newColumn = it.getNewColumn(i);
                    if (!Objects.equals(oldColumn.getColumnName(), newColumn.getColumnName())) {
                        results.add(CompareDiff.Type.COLUMNS_MODIFY.of()
                                .setTargetName(it.getTableName())
                                .setOldDefine(oldColumn.getColumnName())
                                .setNewDefine(newColumn.getColumnName())
                                .setColumnIndex(i)
                                .build());
                    }
                } else {
                    results.add(CompareDiff.Type.COLUMNS_DELETE.of()
                            .setTargetName(it.getTableName())
                            .setOldDefine(oldColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            });
            return results;
        };
    }

    protected Function<TableCompare, List<CompareDiff>> searchAddColumns() {
        return it -> {
            final int newColumns = it.getNewColumnLength();
            final int oldColumns = it.getOldColumnLength();
            if (oldColumns < newColumns) {
                return IntStream.range(oldColumns, newColumns).mapToObj(i -> {
                            final Column newColumn = it.getNewColumn(i);
                            return CompareDiff.Type.COLUMNS_ADD.of()
                                    .setTargetName(it.getTableName())
                                    .setNewDefine(newColumn.getColumnName())
                                    .setColumnIndex(i)
                                    .build();
                        })
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        };
    }

    protected Function<TableCompare, List<CompareDiff>> rowCount() {
        return it -> {
            final List<CompareDiff> results = new ArrayList<>();
            final int newRows = it.getNewTable().getRowCount();
            final int oldRows = it.getOldTable().getRowCount();
            if (oldRows != newRows) {
                results.add(CompareDiff.Type.ROWS_COUNT.of()
                        .setTargetName(it.getTableName())
                        .setRows(Math.abs(oldRows - newRows))
                        .setOldDefine(String.valueOf(oldRows))
                        .setNewDefine(String.valueOf(newRows))
                        .build());
            }
            return results;
        };
    }

    protected Function<TableCompare, List<CompareDiff>> compareRow() {
        return it -> new RowCompare(it, this.getRowResultHandler(it)).exec();
    }

    protected RowCompareResultHandler getRowResultHandler(final TableCompare it) {
        return new DiffWriteRowCompareResultHandler(it).compose(new DataRowCompareResultHandler(it));
    }

    public static class RowCompare {
        protected final ComparableTable oldTable;
        protected final ComparableTable newTable;
        protected final IDataSetConverter converter;
        protected final int columnLength;
        protected List<String> keyColumns;
        protected RowCompareResultHandler handler;

        protected RowCompare(final TableCompare it, final RowCompareResultHandler handler) {
            this.oldTable = it.getOldTable();
            this.newTable = it.getNewTable();
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
