package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class ComparableTable implements ITable {

    private final ITable delegate;

    private final List<Object[]> values;

    public ComparableTable(ITable delegate) throws DataSetException {
        this.delegate = delegate;
        try {
            Field f = delegate.getClass().getDeclaredField("_rowList");
            f.setAccessible(true);
            this.values = (List<Object[]>) f.get(delegate);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }

    }

    @Override
    public ITableMetaData getTableMetaData() {
        return delegate.getTableMetaData();
    }

    public Map<CompareKeys, Object[]> getRows(int columnLength, List<String> keys) throws DataSetException {
        Map<CompareKeys, Object[]> result = Maps.newHashMap();
        for (int rowNum = 0, total = this.values.size(); rowNum < total; rowNum++) {
            result.put(this.getKey(rowNum, keys), this.getRow(rowNum, columnLength));
        }
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString());
        }
        return result;
    }

    public CompareKeys getKey(int rowNum, List<String> keys) throws DataSetException {
        return new CompareKeys(this, rowNum, keys);
    }

    public Object[] getRow(int rowNum, int columnLength) {
        Object[] row = this.values.get(rowNum);
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
        Object[] row = this.values.get(i);
        int j = this.delegate.getTableMetaData().getColumnIndex(s);
        return this.getValue(i, j);
    }

    public Object getValue(int i, int j) {
        Object[] row = this.values.get(i);
        return row[j];
    }

}
