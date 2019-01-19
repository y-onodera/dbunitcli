package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.util.List;
import java.util.Map;

public abstract class AbstractComparableDataSet extends CachedDataSet implements ComparableDataSet {
    public AbstractComparableDataSet(IDataSetProducer producer) throws DataSetException {
        super(producer);
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        return new ComparableTable(super.getTable(tableName));
    }
}
