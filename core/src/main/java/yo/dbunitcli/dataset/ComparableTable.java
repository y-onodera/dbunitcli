package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public record ComparableTable(AddSettingTableMetaData addSettingTableMetaData
        , AddSettingTableMetaData.Rows rows
        , Integer[] _sortedIndexes) implements ITable {

    public ComparableTable(final Builder builder) {
        this(builder.getAddSettingTableMetaData(), builder.getRows(), builder.getSortedIndexes());
    }

    public String getTableName() {
        return this.getTableMetaData().getTableName();
    }

    @Override
    public AddSettingTableMetaData getTableMetaData() {
        return this.addSettingTableMetaData;
    }

    @Override
    public int getRowCount() {
        return this.getRows().size();
    }

    @Override
    public Object getValue(final int i, final String s) {
        try {
            return this.getValue(i, this.getTableMetaData().getColumnIndex(s));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public Stream<Map<String, Object>> stream() {
        return IntStream.range(0, this.getRowCount()).mapToObj(this::getRowToMap);
    }

    public int getNumberOfColumns() {
        return this.getTableMetaData().columns().length;
    }

    public AddSettingTableMetaData.Rows getRows() {
        return this.rows;
    }

    public List<Map<String, Object>> toMap(final boolean includeMetaData) {
        final List<Map<String, Object>> result = new ArrayList<>();
        final Map<String, Object> withMetaDataMap = new HashMap<>();
        final List<Map<String, Object>> rowMap = new ArrayList<>();
        if (includeMetaData) {
            withMetaDataMap.put("tableName", this.getTableMetaData().getTableName());
            withMetaDataMap.put("columns", this.getTableMetaData().getColumns());
            withMetaDataMap.put("primaryKeys", this.getTableMetaData().getPrimaryKeys());
            withMetaDataMap.put("columnsExcludeKey", this.getColumnsExcludeKey());
            withMetaDataMap.put("rows", rowMap);
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
        final List<Column> primaryKey = Arrays.asList(this.getTableMetaData().getPrimaryKeys());
        return Arrays.stream(this.getTableMetaData().getColumns())
                .filter(it -> !primaryKey.contains(it))
                .toArray(Column[]::new);
    }

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(final List<String> keys) {
        final Map<CompareKeys, Map.Entry<Integer, Object[]>> result = new HashMap<>();
        IntStream.range(0, this.getRowCount())
                .forEach(rowNum -> result.put(this.getKey(rowNum, keys)
                        , new AbstractMap.SimpleEntry<>(this.getOriginalRowIndex(rowNum), this.getRow(rowNum))));
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString() + " metaData:" + this.addSettingTableMetaData);
        }
        return result;
    }

    public Object[] get(final CompareKeys targetKey, final List<String> keys, final int columnLength) {
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            if (targetKey.equals(this.getKey(rowNum, keys))) {
                return this.getRow(rowNum, columnLength);
            }
        }
        throw new AssertionError("keys not found:" + targetKey.toString() + ". metaData:" + this.addSettingTableMetaData);
    }

    public CompareKeys getKey(final int rowNum, final List<String> keys) {
        return new CompareKeys(this, rowNum, keys).oldRowNum(this.getOriginalRowIndex(rowNum));
    }

    public Map<String, Object> getRowToMap(final int rowNum) {
        return this.addSettingTableMetaData.rowToMap(this.getRow(rowNum));
    }

    public Object[] getRow(final int rowNum) {
        if (rowNum < 0 || rowNum >= this.getRowCount()) {
            throw new AssertionError("rowNum " + rowNum + " is out of range;current row size is " + this.getRowCount() + ". metaData:" + this.addSettingTableMetaData);
        }
        return this.rows.get(this.getIndexBeforeSort(rowNum));
    }

    public Object[] getRow(final int rowNum, final int columnLength) {
        final Object[] row = this.getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length + " row:" + Arrays.toString(row) + " metaData:" + this.addSettingTableMetaData);
        }
        final Object[] resultRow = new Object[columnLength];
        System.arraycopy(row, 0, resultRow, 0, columnLength);
        return resultRow;
    }

    public Object getValue(final int i, final int j) {
        final Object[] row = this.getRow(i);
        return row[j] == null ? ITable.NO_VALUE : row[j];
    }

    private int getOriginalRowIndex(final int sortedIndex) {
        final int row = this.getIndexBeforeSort(sortedIndex);
        if (this.addSettingTableMetaData.hasRowFilter()) {
            return this.rows.filteredRowIndexes().get(row);
        }
        return row;
    }

    private int getIndexBeforeSort(final int sortedIndex) {
        if (!this.addSettingTableMetaData.isSorted()) {
            return sortedIndex;
        }
        return this._sortedIndexes[sortedIndex];
    }

    public static class Builder {
        private final AddSettingTableMetaData addSettingTableMetaData;
        private AddSettingTableMetaData.Rows rows = new AddSettingTableMetaData.Rows();

        public Builder(final ITableMetaData metaData) {
            this(TableSeparator.NONE.addSetting(metaData));
        }

        public Builder(final AddSettingTableMetaData addSettingTableMetaData) {
            this.addSettingTableMetaData = addSettingTableMetaData;
        }

        public ComparableTable build() {
            return new ComparableTable(this);
        }

        public Builder setRows(final AddSettingTableMetaData.Rows rows) {
            this.rows = rows;
            return this;
        }

        public AddSettingTableMetaData getAddSettingTableMetaData() {
            return this.addSettingTableMetaData;
        }

        public AddSettingTableMetaData.Rows getRows() {
            return this.rows;
        }

        public Integer[] getSortedIndexes() {
            if (this.addSettingTableMetaData.isSorted()) {
                final Integer[] sortedIndexes = new Integer[this.rows.rows().size()];
                for (int i = 0; i < sortedIndexes.length; ++i) {
                    sortedIndexes[i] = i;
                }
                final Integer[] columnIndex = Arrays.stream(this.addSettingTableMetaData.getOrderColumns()).map(it -> {
                            try {
                                return this.addSettingTableMetaData.getColumnIndex(it.getColumnName());
                            } catch (final DataSetException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toArray(Integer[]::new);
                Arrays.sort(sortedIndexes, (final Integer i1, final Integer i2) -> {
                    try {
                        for (final Integer index : columnIndex) {
                            final Object value1 = this.rows.rows().get(i1)[index];
                            final Object value2 = this.rows.rows().get(i2)[index];
                            if (value1 != null || value2 != null) {
                                if (value1 == null) {
                                    return 1;
                                }
                                if (value2 == null) {
                                    return -1;
                                }
                                final int result = this.addSettingTableMetaData.getColumns()[index].getDataType().compare(value1, value2);
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
                return sortedIndexes;
            }
            return null;
        }
    }
}
