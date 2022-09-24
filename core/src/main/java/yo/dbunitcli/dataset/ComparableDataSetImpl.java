package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.*;

import java.util.List;
import java.util.Map;

public class ComparableDataSetImpl extends AbstractDataSet implements ComparableDataSet {

    private static final Logger LOGGER = LogManager.getLogger();

    private ComparableTableMapper mapper;

    private final ColumnSettings compareSettings;

    private final ComparableDataSetParam param;

    private final ComparableDataSetProducer producer;

    private final IDataSetConsumer consumer;

    public ComparableDataSetImpl(ComparableDataSetProducer producer) throws DataSetException {
        super(false);
        this.producer = producer;
        this.param = this.producer.getParam();
        this.compareSettings = this.param.getColumnSettings();
        this.consumer = this.param.getConsumer();
        this.producer.setConsumer(this);
        this.producer.produce();
    }

    @Override
    public void startDataSet() throws DataSetException {
        LOGGER.debug("startDataSet() - start");
        this._orderedTableNameMap = super.createTableNameMap();
        if (this.consumer != null) {
            this.consumer.startDataSet();
        }
    }

    @Override
    public void endDataSet() throws DataSetException {
        LOGGER.debug("endDataSet() - start");
        if (this.consumer != null) {
            ITableIterator itr = this.createIterator(false);
            while (itr.next()) {
                this.consumer.write(itr.getTable());
            }
            this.consumer.endDataSet();
        }
        LOGGER.debug("endDataSet() - the final tableMap is: " + this._orderedTableNameMap);
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.mapper = this.compareSettings.createMapper(metaData);
        this.mapper.setConsumer(this.consumer);
    }

    @Override
    public void endTable() throws DataSetException {
        LOGGER.debug("endTable() - start");
        String resultTableName = this.mapper.getTargetTableName();
        if (this._orderedTableNameMap.containsTable(resultTableName)) {
            ComparableTable existingTable = (ComparableTable) this._orderedTableNameMap.get(resultTableName);
            this.mapper.add(existingTable);
            this._orderedTableNameMap.update(resultTableName, this.mapper.endTable());
        } else {
            ComparableTable result = this.mapper.endTable();
            if (result != null) {
                this._orderedTableNameMap.add(resultTableName, result);
            }
        }
        this.mapper = null;
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        LOGGER.debug("row(values={}) - start", values);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createIterator(reversed={}) - start", reversed);
        }
        return new DefaultTableIterator((ITable[]) (this._orderedTableNameMap.orderedValues().toArray(new ITable[0])));
    }
}
