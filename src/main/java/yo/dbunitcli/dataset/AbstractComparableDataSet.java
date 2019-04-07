package yo.dbunitcli.dataset;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.stream.IDataSetProducer;
import yo.dbunitcli.compare.CompareSetting;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractComparableDataSet extends CachedDataSet implements ComparableDataSet {

    private CompareSetting compareSetting;

    public AbstractComparableDataSet(IDataSetProducer producer) throws DataSetException {
        this(producer, CompareSetting.builder().build());
    }

    public AbstractComparableDataSet(IDataSetProducer producer, CompareSetting excludeColumns) throws DataSetException {
        super(producer);
        this.compareSetting = excludeColumns;
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        List<Column> excludeColumns = this.compareSetting.get(tableName).stream().map(it -> new Column(it, DataType.UNKNOWN)).collect(Collectors.toList());
        if (excludeColumns.size() > 0) {
            return ComparableFilterTable.createFrom(super.getTable(tableName), this.toFilter(excludeColumns));
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

    private DefaultColumnFilter toFilter(List<Column> excludeColumns) {
        DefaultColumnFilter result = new DefaultColumnFilter();
        result.excludeColumns(excludeColumns.toArray(new Column[excludeColumns.size()]));
        return result;
    }
}
