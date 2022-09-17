package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ComparableDataSetImpl extends AbstractDataSet implements ComparableDataSet {

    private static final Logger logger = LoggerFactory.getLogger(ComparableDataSetImpl.class);

    private ComparableTableMapper mapper;

    private final ColumnSettings compareSettings;

    private final ComparableDataSetParam param;

    private final ComparableDataSetProducer producer;

    public ComparableDataSetImpl(ComparableDataSetProducer producer) throws DataSetException {
        super(false);
        this.producer = producer;
        this.param = this.producer.getParam();
        this.compareSettings = this.param.getColumnSettings();
        this.producer.setConsumer(this);
        this.producer.produce();
    }

    @Override
    public void startDataSet() throws DataSetException {
        logger.debug("startDataSet() - start");
        this._orderedTableNameMap = super.createTableNameMap();
    }

    @Override
    public void endDataSet() throws DataSetException {
        logger.debug("endDataSet() - start");
        logger.debug("endDataSet() - the final tableMap is: " + this._orderedTableNameMap);
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        logger.debug("startTable(metaData={}) - start", metaData);
        this.mapper = this.compareSettings.createMapper(metaData);
    }

    @Override
    public void endTable() throws DataSetException {
        logger.debug("endTable() - start");
        ComparableTable result = this.mapper.result();
        String resultTableName = result.getTableMetaData().getTableName();
        if (this._orderedTableNameMap.containsTable(resultTableName)) {
            ComparableTable existingTable = (ComparableTable) this._orderedTableNameMap.get(resultTableName);
            existingTable.addTableRows(result);
        } else {
            this._orderedTableNameMap.add(resultTableName, result);
        }
        this.mapper = null;
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        logger.debug("row(values={}) - start", values);
        this.mapper.addRow(values);
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
        return (ComparableTable) super.getTable(tableName);
    }

    @Override
    public boolean contains(String tableName) {
        return this._orderedTableNameMap.containsTable(tableName);
    }

    @Override
    public String getSrc() {
        return this.producer.getSrc();
    }

    @Override
    protected ITableIterator createIterator(boolean reversed) {
        if (logger.isDebugEnabled()) {
            logger.debug("createIterator(reversed={}) - start", reversed);
        }
        return new DefaultTableIterator((ITable[]) (this._orderedTableNameMap.orderedValues().toArray(new ITable[0])), reversed);
    }
}
