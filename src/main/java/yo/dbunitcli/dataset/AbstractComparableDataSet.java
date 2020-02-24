package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.util.List;
import java.util.Map;

public abstract class AbstractComparableDataSet extends CachedDataSet implements ComparableDataSet {

    private final ColumnSettings compareSettings;

    private final ComparableDataSetLoaderParam param;

    public AbstractComparableDataSet(IDataSetProducer producer) throws DataSetException {
        this(producer, ComparableDataSetLoaderParam.builder().build());
    }

    public AbstractComparableDataSet(IDataSetProducer producer, ComparableDataSetLoaderParam param) throws DataSetException {
        super(producer);
        this.param = param;
        this.compareSettings = this.param.getColumnSettings();
    }

    @Override
    public List<Map<String, Object>> toMap() throws DataSetException {
        return this.toMap(this.param.isMapIncludeMetaData());
    }

    @Override
    public List<Map<String, Object>> toMap(boolean includeMetaData) throws DataSetException {
        List<Map<String, Object>> result = Lists.newArrayList();
        for (String tableName : this.getTableNames()) {
            ComparableTable table = this.getTable(tableName);
            result.addAll(table.toMap(includeMetaData));
        }
        return result;
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        List<Column> excludeColumns = this.compareSettings.getExcludeColumns(tableName);
        if (excludeColumns.size() > 0) {
            return ComparableFilterTable.createFrom(super.getTable(tableName)
                    , this.orderColumns(tableName)
                    , this.columnExpression(tableName)
                    , this.toFilter(excludeColumns));
        }
        return ComparableTable.createFrom(super.getTable(tableName)
                , this.orderColumns(tableName)
                , this.columnExpression(tableName));
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

    private ColumnExpression columnExpression(String tableName) {
        return this.compareSettings.getExpression(tableName);
    }

    private Column[] orderColumns(String tableName) {
        return this.compareSettings.getOrderColumns(tableName);
    }

    private DefaultColumnFilter toFilter(List<Column> excludeColumns) {
        DefaultColumnFilter result = new DefaultColumnFilter();
        result.excludeColumns(excludeColumns.toArray(new Column[0]));
        return result;
    }
}
