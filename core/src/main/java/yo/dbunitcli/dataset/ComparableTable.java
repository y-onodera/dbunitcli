package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;


public class ComparableTable implements ITable {

    private final AddSettingTableMetaData addSettingTableMetaData;

    private final Column[] orderColumns;

    private final List<Object[]> values = new ArrayList<>();

    private final List<Integer> filteredRowIndexes = new ArrayList<>();

    private Integer[] _indexes;

    protected ComparableTable(ITableMetaData metaData) throws DataSetException {
        this(ColumnExpression.builder().build().apply(metaData), new Column[]{}, new ArrayList<>(), new ArrayList<>());
    }

    protected ComparableTable(AddSettingTableMetaData addSettingTableMetaData, Column[] orderColumns, List<Object[]> values, List<Integer> filteredRowIndexes) {
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
    public Object getValue(int i, String s) throws DataSetException {
        return this.getValue(i, this.addSettingTableMetaData.getColumnIndex(s));
    }

    public List<Map<String, Object>> toMap() {
        return this.toMap(false);
    }

    public List<Map<String, Object>> toMap(boolean includeMetaData) {
        List<Map<String, Object>> result = new ArrayList<>();
        String tableName = this.getTableMetaData().getTableName();
        Map<String, Object> withMetaDataMap = new HashMap<>();
        if (includeMetaData) {
            withMetaDataMap.put("tableName", tableName);
            withMetaDataMap.put("columns", this.addSettingTableMetaData.getColumns());
            withMetaDataMap.put("primaryKeys", this.addSettingTableMetaData.getPrimaryKeys());
            result.add(withMetaDataMap);
        }
        List<Map<String, Object>> rowMap = new ArrayList<>();
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            Map<String, Object> map = this.getRowToMap(rowNum);
            if (includeMetaData) {
                rowMap.add(map);
            } else {
                result.add(map);
            }
        }
        if (includeMetaData) {
            withMetaDataMap.put("row", rowMap);
        }
        return result;
    }

    public Column[] getColumnsExcludeKey() throws DataSetException {
        List<Column> primaryKey = Arrays.asList(this.getTableMetaData().getPrimaryKeys());
        Column[] columns = this.getTableMetaData().getColumns();
        List<Column> list = new ArrayList<>();
        for (Column it : columns) {
            if (!primaryKey.contains(it)) {
                list.add(it);
            }
        }
        return list.toArray(new Column[0]);
    }

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(List<String> keys) {
        Map<CompareKeys, Map.Entry<Integer, Object[]>> result = new HashMap<>();
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            result.put(this.getKey(rowNum, keys), new AbstractMap.SimpleEntry<>(this.getOriginalRowIndex(rowNum), this.getRow(rowNum)));
        }
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString());
        }
        return result;
    }

    public Object[] get(CompareKeys targetKey, List<String> keys, int columnLength) {
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            if (targetKey.equals(this.getKey(rowNum, keys))) {
                return getRow(rowNum, columnLength);
            }
        }
        throw new AssertionError("keys not found:" + targetKey.toString());
    }

    public CompareKeys getKey(int rowNum, List<String> keys) {
        return new CompareKeys(this, rowNum, keys).oldRowNum(this.getOriginalRowIndex(rowNum));
    }

    public Map<String, Object> getRowToMap(int rowNum) {
        return this.addSettingTableMetaData.rowToMap(this.getRow(rowNum));
    }

    public Object[] getRow(int rowNum) {
        if (rowNum < 0 || rowNum >= this.getRowCount()) {
            throw new AssertionError("rowNum " + rowNum + " is out of range;current row size is " + this.getRowCount());
        }
        return this.values.get(this.getSortedRowIndex(rowNum));
    }

    public Object[] getRow(int rowNum, int columnLength) {
        Object[] row = this.getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length);
        }
        Object[] resultRow = new Object[columnLength];
        System.arraycopy(row, 0, resultRow, 0, columnLength);
        return resultRow;
    }

    public Object getValue(int i, int j) {
        Object[] row = this.getRow(i);
        return row[j] == null ? NO_VALUE : row[j];
    }

    protected int getOriginalRowIndex(int noSort) {
        int row = this.getSortedRowIndex(noSort);
        if (this.addSettingTableMetaData.hasRowFilter()) {
            return this.filteredRowIndexes.get(row);
        }
        return row;
    }

    protected int getSortedRowIndex(int noSort) {
        if (!this.isSorted()) {
            return noSort;
        }
        if (this._indexes == null) {
            Integer[] indexes = new Integer[this.values.size()];
            for (int i = 0; i < indexes.length; ++i) {
                indexes[i] = i;
            }
            Integer[] columnIndex = Arrays.stream(this.orderColumns).map(it -> {
                        try {
                            return addSettingTableMetaData.getColumnIndex(it.getColumnName());
                        } catch (DataSetException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(Integer[]::new);
            Arrays.sort(indexes, (Integer i1, Integer i2) -> {
                try {
                    for (int i = 0, j = columnIndex.length; i < j; i++) {
                        Object value1 = values.get(i1)[columnIndex[i]];
                        Object value2 = values.get(i2)[columnIndex[i]];
                        if (value1 != null || value2 != null) {
                            if (value1 == null) {
                                return -1;
                            }
                            if (value2 == null) {
                                return 1;
                            }
                            int result = addSettingTableMetaData.getColumns()[i].getDataType().compare(value1, value2);
                            if (result != 0) {
                                return result;
                            }
                        }
                    }
                    return 0;
                } catch (DataSetException var10) {
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
