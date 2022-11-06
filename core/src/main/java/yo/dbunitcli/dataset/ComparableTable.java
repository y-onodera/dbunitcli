package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.stream.IntStream;


public class ComparableTable implements ITable {

    private final AddSettingTableMetaData addSettingTableMetaData;

    private final Column[] orderColumns;

    private final List<Object[]> values = new ArrayList<>();

    private final List<Integer> filteredRowIndexes = new ArrayList<>();

    private Integer[] _indexes;

    protected ComparableTable(final ITableMetaData metaData) {
        this(ColumnExpression.builder().build().apply(metaData), new Column[]{}, new ArrayList<>(), new ArrayList<>());
    }

    protected ComparableTable(final AddSettingTableMetaData addSettingTableMetaData, final Column[] orderColumns, final List<Object[]> values, final List<Integer> filteredRowIndexes) {
        this.addSettingTableMetaData = addSettingTableMetaData;
        this.orderColumns = orderColumns;
        this.values.addAll(values);
        this.filteredRowIndexes.addAll(filteredRowIndexes);
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return this.addSettingTableMetaData;
    }

    @Override
    public int getRowCount() {
        if (this.addSettingTableMetaData.hasRowFilter()) {
            return this.filteredRowIndexes.size();
        }
        return this.values.size();
    }

    @Override
    public Object getValue(final int i, final String s) {
        try {
            return this.getValue(i, this.addSettingTableMetaData.getColumnIndex(s));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public List<Map<String, Object>> toMap() {
        return this.toMap(false);
    }

    public List<Map<String, Object>> toMap(final boolean includeMetaData) {
        final List<Map<String, Object>> result = new ArrayList<>();
        final Map<String, Object> withMetaDataMap = new HashMap<>();
        final List<Map<String, Object>> rowMap = new ArrayList<>();
        if (includeMetaData) {
            withMetaDataMap.put("tableName", this.getTableMetaData().getTableName());
            withMetaDataMap.put("columns", this.addSettingTableMetaData.getColumns());
            withMetaDataMap.put("primaryKeys", this.addSettingTableMetaData.getPrimaryKeys());
            withMetaDataMap.put("row", rowMap);
            result.add(withMetaDataMap);
        }
        IntStream.range(0, this.getRowCount()).forEach(rowNum -> {
            final Map<String, Object> map = this.getRowToMap(rowNum);
            if (includeMetaData) {
                rowMap.add(map);
            } else {
                result.add(map);
            }
        });
        return result;
    }

    public Column[] getColumnsExcludeKey() {
        try {
            final List<Column> primaryKey = Arrays.asList(this.getTableMetaData().getPrimaryKeys());
            return Arrays.stream(this.getTableMetaData().getColumns())
                    .filter(it -> !primaryKey.contains(it))
                    .toArray(Column[]::new);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(final List<String> keys) {
        final Map<CompareKeys, Map.Entry<Integer, Object[]>> result = new HashMap<>();
        IntStream.range(0, this.getRowCount())
                .forEach(rowNum -> result.put(this.getKey(rowNum, keys)
                        , new AbstractMap.SimpleEntry<>(this.getOriginalRowIndex(rowNum), this.getRow(rowNum))));
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString());
        }
        return result;
    }

    public Object[] get(final CompareKeys targetKey, final List<String> keys, final int columnLength) {
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            if (targetKey.equals(this.getKey(rowNum, keys))) {
                return this.getRow(rowNum, columnLength);
            }
        }
        throw new AssertionError("keys not found:" + targetKey.toString());
    }

    public CompareKeys getKey(final int rowNum, final List<String> keys) {
        return new CompareKeys(this, rowNum, keys).oldRowNum(this.getOriginalRowIndex(rowNum));
    }

    public Map<String, Object> getRowToMap(final int rowNum) {
        return this.addSettingTableMetaData.rowToMap(this.getRow(rowNum));
    }

    public Object[] getRow(final int rowNum) {
        if (rowNum < 0 || rowNum >= this.getRowCount()) {
            throw new AssertionError("rowNum " + rowNum + " is out of range;current row size is " + this.getRowCount());
        }
        return this.values.get(this.getSortedRowIndex(rowNum));
    }

    public Object[] getRow(final int rowNum, final int columnLength) {
        final Object[] row = this.getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length);
        }
        final Object[] resultRow = new Object[columnLength];
        System.arraycopy(row, 0, resultRow, 0, columnLength);
        return resultRow;
    }

    public Object getValue(final int i, final int j) {
        final Object[] row = this.getRow(i);
        return row[j] == null ? NO_VALUE : row[j];
    }

    protected int getOriginalRowIndex(final int noSort) {
        final int row = this.getSortedRowIndex(noSort);
        if (this.addSettingTableMetaData.hasRowFilter()) {
            return this.filteredRowIndexes.get(row);
        }
        return row;
    }

    protected int getSortedRowIndex(final int noSort) {
        if (!this.isSorted()) {
            return noSort;
        }
        if (this._indexes == null) {
            final Integer[] indexes = new Integer[this.values.size()];
            for (int i = 0; i < indexes.length; ++i) {
                indexes[i] = i;
            }
            final Integer[] columnIndex = Arrays.stream(this.orderColumns).map(it -> {
                        try {
                            return this.addSettingTableMetaData.getColumnIndex(it.getColumnName());
                        } catch (final DataSetException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(Integer[]::new);
            Arrays.sort(indexes, (Integer i1, Integer i2) -> {
                try {
                    for (int i = 0, j = columnIndex.length; i < j; i++) {
                        final Object value1 = this.values.get(i1)[columnIndex[i]];
                        final Object value2 = this.values.get(i2)[columnIndex[i]];
                        if (value1 != null || value2 != null) {
                            if (value1 == null) {
                                return -1;
                            }
                            if (value2 == null) {
                                return 1;
                            }
                            final int result = this.addSettingTableMetaData.getColumns()[i].getDataType().compare(value1, value2);
                            if (result != 0) {
                                return result;
                            }
                        }
                    }
                    return 0;
                } catch (final DataSetException var10) {
                    throw new DatabaseUnitRuntimeException(var10);
                }
            });
            this._indexes = indexes;
        }
        return this._indexes[noSort];
    }

    protected boolean isSorted() {
        return this.orderColumns.length > 0;
    }

}
