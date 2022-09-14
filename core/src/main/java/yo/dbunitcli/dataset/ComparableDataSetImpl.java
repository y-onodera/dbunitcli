package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ComparableDataSetImpl extends AbstractDataSet implements ComparableDataSet {

    private static final Logger logger = LoggerFactory.getLogger(ComparableDataSetImpl.class);

    private DefaultTable _activeTable;

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
        this._activeTable = new DefaultTable(metaData);
    }

    @Override
    public void endTable() throws DataSetException {
        logger.debug("endTable() - start");
        ComparableTable table = this.compareSettings.apply(this._activeTable);
        String resultTableName = table.getTableMetaData().getTableName();
        if (this._orderedTableNameMap.containsTable(resultTableName)) {
            ComparableTable existingTable = (ComparableTable) this._orderedTableNameMap.get(resultTableName);
            existingTable.addTableRows(table);
        } else {
            this._orderedTableNameMap.add(resultTableName, table);
        }
        this._activeTable = null;
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        logger.debug("row(values={}) - start", values);
        this._activeTable.addRow(values);
    }

    @Override
    public List<Map<String, Object>> toMap() throws DataSetException {
        return this.toMap(this.getParam().isMapIncludeMetaData());
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
        try {
            this.getTable(tableName);
            return true;
        } catch (DataSetException e) {
            return false;
        }
    }

    @Override
    public ColumnSettings getCompareSettings() {
        return this.compareSettings;
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public ComparableDataSetProducer getProducer() {
        return this.producer;
    }

    @Override
    protected ITableIterator createIterator(boolean reversed) throws DataSetException {
        if (logger.isDebugEnabled()) {
            logger.debug("createIterator(reversed={}) - start", String.valueOf(reversed));
        }

        ITable[] tables = (ITable[])(this._orderedTableNameMap.orderedValues().toArray(new ITable[0]));
        return new DefaultTableIterator(tables, reversed);
    }
}
