package yo.dbunitcli.dataset.writer;

import com.google.common.collect.Maps;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.Map;
import java.util.Objects;

public class UnknownBlankToNullITable implements ITable {
    private final ITable delegate;
    Map<String, DataType> columnDataType = Maps.newHashMap();

    public UnknownBlankToNullITable(ITable delegate) throws DataSetException {
        this.delegate = delegate;
        for (Column col : this.getTableMetaData().getColumns()) {
            this.columnDataType.put(col.getColumnName(), col.getDataType());
        }
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return delegate.getTableMetaData();
    }

    @Override
    public int getRowCount() {
        return delegate.getRowCount();
    }

    @Override
    public Object getValue(int i, String s) throws DataSetException {
        final Object retVal = delegate.getValue(i, s);
        if (this.columnDataType.get(s) == DataType.UNKNOWN && Objects.equals(retVal, "")) {
            return null;
        }
        return retVal;
    }
}
