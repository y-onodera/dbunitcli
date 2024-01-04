package yo.dbunitcli.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComparableDataSetImpl extends AbstractDataSet implements ComparableDataSet {

    private static final Logger LOGGER = LogManager.getLogger();

    private ComparableTableMapper mapper;

    private final TableSeparators tableSeparators;

    private final ComparableDataSetParam param;

    private final ComparableDataSetProducer producer;

    private final IDataSetConverter converter;

    private final Map<String, Integer> alreadyWrite;

    private final List<ComparableTableJoin> joins;

    public ComparableDataSetImpl(final ComparableDataSetProducer producer) {
        super(false);
        this.producer = producer;
        this.param = this.producer.getParam();
        this.tableSeparators = this.param.tableSeparators();
        this.converter = this.param.converter();
        this.alreadyWrite = new HashMap<>();
        this.joins = this.tableSeparators.joins()
                .stream()
                .map(ComparableTableJoin::new)
                .collect(Collectors.toList());
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
        if (this.joins.size() > 0) {
            final List<String> joinSource = this.joins.stream()
                    .flatMap(it -> Stream.of(it.getCondition().outer(), it.getCondition().inner()))
                    .toList();
            final OrderedTableNameMap excludeJoinSource = super.createTableNameMap();
            for (final String tableName : this._orderedTableNameMap.getTableNames()) {
                if (!joinSource.contains(tableName)) {
                    excludeJoinSource.add(tableName, this._orderedTableNameMap.get(tableName));
                }
            }
            this._orderedTableNameMap = excludeJoinSource;
            new ArrayList<>(this.joins).forEach(it -> {
                this.joins.remove(it);
                this.startTable(it.createMetaData());
                it.joinRows().forEach(this::row);
                this.endTable();
            });
        }
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
    public void startTable(final ITableMetaData metaData) {
        LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.mapper = this.tableSeparators.createMapper(metaData);
        this.mapper.startTable(this.converter, this.alreadyWrite, this.joins);
    }

    @Override
    public void row(final Object[] values) {
        LOGGER.debug("row(values={}) - start", values);
        this.mapper.addRow(values);
    }

    @Override
    public void endTable() {
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
