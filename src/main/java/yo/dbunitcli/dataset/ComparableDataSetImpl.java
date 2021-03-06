package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;

import java.util.List;
import java.util.Map;

public class ComparableDataSetImpl extends CachedDataSet implements ComparableDataSet {

    private final ColumnSettings compareSettings;

    private final ComparableDataSetParam param;

    private final ComparableDataSetProducer producer;

    public ComparableDataSetImpl(ComparableDataSetProducer producer) throws DataSetException {
        super(producer);
        this.producer = producer;
        this.param = this.producer.getParam();
        this.compareSettings = this.param.getColumnSettings();
    }

    @Override
    public String getSrc() {
        return this.producer.getSrc();
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
        return this.compareSettings.apply(super.getTable(tableName));
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
