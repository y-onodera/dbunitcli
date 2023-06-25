package yo.dbunitcli.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ComparableDataSetImpl extends AbstractDataSet implements ComparableDataSet {

    private static final Logger LOGGER = LogManager.getLogger();

    private ComparableTableMapper mapper;

    private final ColumnSettings compareSettings;

    private final ComparableDataSetParam param;

    private final ComparableDataSetProducer producer;

    private final IDataSetConverter converter;

    private final Map<String, Integer> alreadyWrite;

    public ComparableDataSetImpl(final ComparableDataSetProducer producer) {
        super(false);
        this.producer = producer;
        this.param = this.producer.getParam();
        this.compareSettings = this.param.columnSettings();
        this.converter = this.param.converter();
        this.alreadyWrite = new HashMap<>();
        try {
            this.producer.setConsumer(this);
            this.producer.produce();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void startDataSet() throws DataSetException {
        LOGGER.debug("startDataSet() - start");
        this._orderedTableNameMap = super.createTableNameMap();
        if (this.converter != null) {
            this.converter.startDataSet();
        }
    }

    @Override
    public void endDataSet() throws DataSetException {
        LOGGER.debug("endDataSet() - start");
        if (this.converter != null) {
            final ITableIterator itr = this.createIterator(false);
            while (itr.next()) {
                this.converter.convert(itr.getTable());
            }
            this.converter.endDataSet();
        }
        LOGGER.debug("endDataSet() - the final tableMap is: " + this._orderedTableNameMap);
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.mapper = this.compareSettings.createMapper(metaData);
        this.mapper.startTable(this.converter, this.alreadyWrite);
    }

    @Override
    public void row(final Object[] values) throws DataSetException {
        LOGGER.debug("row(values={}) - start", values);
        this.mapper.addRow(values);
    }

    @Override
    public void endTable() throws DataSetException {
        LOGGER.debug("endTable() - start");
        this.mapper.endTable(this._orderedTableNameMap);
        this.mapper = null;
    }

    @Override
    public Stream<Map<String, Object>> toMap() {
        return this.toMap(this.param.mapIncludeMetaData());
    }

    @Override
    public Stream<Map<String, Object>> toMap(final boolean includeMetaData) {
        try {
            return Arrays.stream(this.getTableNames()).map(tableName -> this.getTable(tableName).toMap(includeMetaData))
                    .flatMap(Collection::stream);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public ComparableTable getTable(final String tableName) {
        try {
            return (ComparableTable) super.getTable(tableName);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean contains(final String tableName) {
        return this._orderedTableNameMap.containsTable(tableName);
    }

    @Override
    public String getSrc() {
        return this.producer.getSrc();
    }

    @Override
    protected ITableIterator createIterator(final boolean reversed) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("createIterator(reversed={}) - start", reversed);
        }
        return new DefaultTableIterator((ITable[]) (this._orderedTableNameMap.orderedValues().toArray(new ITable[0])));
    }
}
