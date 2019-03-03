package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.util.Map;

public abstract class AbstractComparableDataSet extends CachedDataSet implements ComparableDataSet {

    private Map<String, IColumnFilter> columnFilters;

    public AbstractComparableDataSet(IDataSetProducer producer, Map<String, IColumnFilter> aColumnFilters) throws DataSetException {
        super(producer);
        this.columnFilters = aColumnFilters;
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        if (this.columnFilters.containsKey(tableName)) {
            return ComparableFilterTable.createFrom(super.getTable(tableName), columnFilters.get(tableName));
        }
        return ComparableTable.createFrom(super.getTable(tableName));
    }

    @Override
    public boolean contains(String tableName) {
        try {
            this.getTable(tableName);
            return true;
        } catch (DataSetException e) {
            return false;
        }
    }
}
