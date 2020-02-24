package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.TypeCastException;

import java.lang.reflect.Field;
import java.util.*;


public class ComparableTable implements ITable {

    private Integer[] _indexes;

    private final Comparator<Object> rowComparator;

    private final List<Object[]> values;

    private final AddExpressionTableMetaData addExpressionTableMetaData;

    public static ComparableTable createFrom(ITable table) throws DataSetException {
        return createFrom(table, new Column[0], ColumnExpression.builder().build());
    }

    public static ComparableTable createFrom(ITable table, Column[] orderColumns, ColumnExpression additionalExpression) throws DataSetException {
        try {
            return new ComparableTable(additionalExpression.apply(table.getTableMetaData())
                    , getOriginRows(table)
                    , getComparator(table, orderColumns));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
    }

    protected ComparableTable(AddExpressionTableMetaData tableMetaData, List<Object[]> values, Comparator<Object> comparator) {
        this.addExpressionTableMetaData = tableMetaData;
        this.values = values;
        this.rowComparator = comparator;
    }

    public boolean isSorted() {
        return this.rowComparator != null;
    }

    public List<Map<String, Object>> toMap() throws DataSetException {
        List<Map<String, Object>> result = Lists.newArrayList();
        String tableName = this.getTableMetaData().getTableName();
        Column[] columns = this.getTableMetaData().getColumns();
        for (int rowNum = 0, total = this.values.size(); rowNum < total; rowNum++) {
            Object[] row = this.getRow(rowNum);
            Map<String, Object> map = Maps.newHashMap();
            map.put("_tableName", tableName);
            for (int i = 0, j = row.length; i < j; i++) {
                map.put(columns[i].getColumnName(), row[i]);
            }
            result.add(map);
        }
        return result;
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return this.addExpressionTableMetaData;
    }

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(List<String> keys) throws DataSetException {
        Map<CompareKeys, Map.Entry<Integer, Object[]>> result = Maps.newHashMap();
        for (int rowNum = 0, total = this.values.size(); rowNum < total; rowNum++) {
            result.put(this.getKey(rowNum, keys), new AbstractMap.SimpleEntry<>(this.getOriginalRowIndex(rowNum), this.getRow(rowNum)));
        }
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString());
        }
        return result;
    }

    public Object[] get(CompareKeys targetKey, List<String> keys, int columnLength) throws DataSetException {
        for (int rowNum = 0, total = this.values.size(); rowNum < total; rowNum++) {
            if (targetKey.equals(this.getKey(rowNum, keys))) {
                return getRow(rowNum, columnLength);
            }
        }
        throw new AssertionError("keys not found:" + targetKey.toString());
    }

    public CompareKeys getKey(int rowNum, List<String> keys) throws DataSetException {
        return new CompareKeys(this, rowNum, keys).oldRowNum(this.getOriginalRowIndex(rowNum));
    }

    public Object[] getRow(int rowNum, int columnLength) throws RowOutOfBoundsException {
        Object[] row = getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length);
        }
        Object[] resultRow = new Object[columnLength];
        System.arraycopy(row, 0, resultRow, 0, columnLength);
        return resultRow;
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public Object getValue(int i, String s) throws DataSetException {
        int j = this.addExpressionTableMetaData.getColumnIndex(s);
        return this.getValue(i, j);
    }

    public Object getValue(int i, int j) throws RowOutOfBoundsException {
        Object[] row = this.getRow(i);
        return row[j] == null ? "" : row[j];
    }

    public Object[] getRow(int rowNum) throws RowOutOfBoundsException {
        if (rowNum < 0 || rowNum >= this.values.size()) {
            throw new RowOutOfBoundsException("rowNum " + rowNum + " is out of range;current row size is " + this.values.size());
        }
        return addExpressionTableMetaData.applyExpression(this.values.get(this.getOriginalRowIndex(rowNum)));
    }

    protected void addRow(Object[] row) {
        this.values.add(row);
    }

    protected void replaceValue(int row, int column, Object newValue) throws RowOutOfBoundsException {
        this.getRow(row)[column] = newValue;
    }

    protected ITableMetaData getAddExpressionTableMetaData() {
        return this.addExpressionTableMetaData;
    }

    protected int getOriginalRowIndex(int row) {
        if (!this.isSorted()) {
            return row;
        }
        if (this._indexes == null) {
            Integer[] indexes = new Integer[this.getRowCount()];
            for (int i = 0; i < indexes.length; ++i) {
                indexes[i] = i;
            }
            Arrays.sort(indexes, this.rowComparator);
            this._indexes = indexes;
        }
        return this._indexes[row];
    }

    protected static List<Object[]> getOriginRows(ITable delegate) throws NoSuchFieldException, IllegalAccessException {
        Field f = delegate.getClass().getDeclaredField("_rowList");
        f.setAccessible(true);
        return (List<Object[]>) f.get(delegate);
    }

    protected static Comparator<Object> getComparator(ITable delegate, Column[] orderColumns) {
        if (orderColumns.length > 0) {
            return new SortedTable.AbstractRowComparator(delegate, orderColumns) {
                @Override
                protected int compare(Column column, Object o, Object o1) throws TypeCastException {
                    return column.getDataType().compare(o, o1);
                }
            };
        }
        return null;
    }

}
