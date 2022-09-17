package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.*;

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

    public List<Map<String, Object>> toMap() throws RowOutOfBoundsException {
        return this.toMap(false);
    }

    public List<Map<String, Object>> toMap(boolean includeMetaData) throws RowOutOfBoundsException {
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

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(List<String> keys) throws DataSetException {
        Map<CompareKeys, Map.Entry<Integer, Object[]>> result = new HashMap<>();
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            result.put(this.getKey(rowNum, keys), new AbstractMap.SimpleEntry<>(this.getOriginalRowIndex(rowNum), this.getRow(rowNum)));
        }
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString());
        }
        return result;
    }

    public Object[] get(CompareKeys targetKey, List<String> keys, int columnLength) throws DataSetException {
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            if (targetKey.equals(this.getKey(rowNum, keys))) {
                return getRow(rowNum, columnLength);
            }
        }
        throw new AssertionError("keys not found:" + targetKey.toString());
    }

    public CompareKeys getKey(int rowNum, List<String> keys) throws DataSetException {
        return new CompareKeys(this, rowNum, keys).oldRowNum(this.getOriginalRowIndex(rowNum));
    }

    public Map<String, Object> getRowToMap(int rowNum) throws RowOutOfBoundsException {
        return this.addSettingTableMetaData.rowToMap(this.getRow(rowNum));
    }

    public Object[] getRow(int rowNum) throws RowOutOfBoundsException {
        if (rowNum < 0 || rowNum >= this.getRowCount()) {
            throw new RowOutOfBoundsException("rowNum " + rowNum + " is out of range;current row size is " + this.getRowCount());
        }
        return this.addSettingTableMetaData.applySetting(this.values.get(this.getOriginalRowIndex(rowNum)));
    }

    public Object[] getRow(int rowNum, int columnLength) throws RowOutOfBoundsException {
        Object[] row = this.getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length);
        }
        Object[] resultRow = new Object[columnLength];
        System.arraycopy(row, 0, resultRow, 0, columnLength);
        return resultRow;
    }

    public Object getValue(int i, int j) throws RowOutOfBoundsException {
        Object[] row = this.getRow(i);
        return row[j] == null ? "" : row[j];
    }

    protected int getOriginalRowIndex(int noFilter) {
        int row = noFilter;
        if (this.addSettingTableMetaData.hasRowFilter()) {
            row = this.filteredRowIndexes.get(noFilter);
        }
        if (!this.isSorted()) {
            return row;
        }
        if (this._indexes == null) {
            Integer[] indexes = new Integer[this.values.size()];
            for (int i = 0; i < indexes.length; ++i) {
                indexes[i] = i;
            }
            List<Object[]> rows = new ArrayList<>();
            for (int i = 0, j = this.values.size(); i < j; i++) {
                if (!this.addSettingTableMetaData.hasRowFilter() || this.filteredRowIndexes.contains(i)) {
                    rows.add(this.addSettingTableMetaData.applySetting(this.values.get(i)));
                }
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
                        Object value1 = rows.get(i1)[columnIndex[i]];
                        Object value2 = rows.get(i2)[columnIndex[i]];
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
        return this._indexes[row];
    }

    protected boolean isSorted() {
        return this.orderColumns.length > 0;
    }

}
