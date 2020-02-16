package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractComparableDataSet extends CachedDataSet implements ComparableDataSet {

    private ColumnSetting compareSetting;

    private ColumnSetting orderSetting;

    public AbstractComparableDataSet(IDataSetProducer producer) throws DataSetException {
        this(producer, ColumnSetting.builder().build(), ColumnSetting.builder().build());
    }

    public AbstractComparableDataSet(IDataSetProducer producer, ColumnSetting excludeColumns, ColumnSetting orderColumns) throws DataSetException {
        super(producer);
        this.compareSetting = excludeColumns;
        this.orderSetting = orderColumns;
    }

    @Override
    public List<Map<String, Object>> toMap() throws DataSetException {
        List<Map<String, Object>> result = Lists.newArrayList();
        for (String tableName : this.getTableNames()) {
            ComparableTable table = this.getTable(tableName);
            result.addAll(table.toMap());
        }
        return result;
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        List<Column> excludeColumns = this.compareSetting.getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
        if (excludeColumns.size() > 0) {
            return ComparableFilterTable.createFrom(super.getTable(tableName)
                    , this.orderColumns(tableName)
                    , this.toFilter(excludeColumns));
        }
        return ComparableTable.createFrom(super.getTable(tableName), this.orderColumns(tableName));
    }

    private Column[] orderColumns(String tableName) {
        return this.orderSetting.getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
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
