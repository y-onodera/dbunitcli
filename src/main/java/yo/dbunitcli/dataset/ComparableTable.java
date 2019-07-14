package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.RowOutOfBoundsException;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;


public class ComparableTable implements ITable {

    private final ITable delegate;

    private final List<Object[]> values;

    public static ComparableTable createFrom(ITable delegate) throws DataSetException {
        try {
            return new ComparableTable(delegate, getOriginRows(delegate));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
    }

    protected ComparableTable(ITable delegate, List<Object[]> values) {
        this.delegate = delegate;
        this.values = values;
    }


    @Override
    public ITableMetaData getTableMetaData() {
        return delegate.getTableMetaData();
    }

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(List<String> keys) throws DataSetException {
        Map<CompareKeys, Map.Entry<Integer, Object[]>> result = Maps.newHashMap();
        for (int rowNum = 0, total = this.values.size(); rowNum < total; rowNum++) {
            result.put(this.getKey(rowNum, keys), new AbstractMap.SimpleEntry(rowNum, this.getRow(rowNum)));
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
        return new CompareKeys(this, rowNum, keys);
    }

    public Object[] getRow(int rowNum, int columnLength) throws RowOutOfBoundsException {
        Object[] row = getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length);
        }
        Object[] resultRow = new Object[columnLength];
        for (int i = 0, j = columnLength; i < j; i++) {
            resultRow[i] = row[i];
        }
        return resultRow;
    }

    @Override
    public int getRowCount() {
        return this.delegate.getRowCount();
    }

    @Override
    public Object getValue(int i, String s) throws DataSetException {
        Object[] row = this.getRow(i);
        int j = this.delegate.getTableMetaData().getColumnIndex(s);
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
        return this.values.get(rowNum);
    }

    protected ITable getDelegate() {
        return delegate;
    }

    protected List<Object[]> getValues() {
        return values;
    }

    protected static List<Object[]> getOriginRows(ITable delegate) throws NoSuchFieldException, IllegalAccessException {
        Field f = delegate.getClass().getDeclaredField("_rowList");
        f.setAccessible(true);
        return (List<Object[]>) f.get(delegate);
    }
}
