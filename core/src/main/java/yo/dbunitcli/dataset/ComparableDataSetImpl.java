package yo.dbunitcli.dataset;

import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComparableDataSetImpl extends AbstractDataSet implements ComparableDataSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableDataSetImpl.class);
    private final TableSeparators tableSeparators;
    private final ComparableDataSetParam param;
    private final IDataSetConverter converter;
    private final Map<String, Integer> alreadyWrite;
    private final List<ComparableTableJoin> joins;
    private final String src;
    private ComparableTableMapper mapper;

    public ComparableDataSetImpl(final ComparableDataSetProducer producer) {
        super(false);
        this.src = producer.getSrc();
        this.param = producer.getParam();
        this.tableSeparators = this.param.tableSeparators();
        this.converter = this.param.converter();
        this.alreadyWrite = new HashMap<>();
        this.joins = this.tableSeparators.joins()
                .stream()
                .map(ComparableTableJoin::new)
                .collect(Collectors.toList());
        try {
            producer.setConsumer(this);
            producer.produce();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void startDataSet() throws DataSetException {
        ComparableDataSetImpl.LOGGER.debug("startDataSet() - start");
        this._orderedTableNameMap = super.createTableNameMap();
        if (this.converter != null) {
            this.converter.startDataSet();
        }
    }

    @Override
    public void endDataSet() throws DataSetException {
        ComparableDataSetImpl.LOGGER.debug("endDataSet() - start");
        this.executeJoin();
        if (this.converter != null) {
            final ITableIterator itr = this.createIterator(false);
            while (itr.next()) {
                this.converter.convert(itr.getTable());
            }
            this.converter.endDataSet();
        }
        ComparableDataSetImpl.LOGGER.debug("endDataSet() - the final tableMap is: " + this._orderedTableNameMap);
    }

    @Override
    public void startTable(final ITableMetaData metaData) {
        ComparableDataSetImpl.LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.mapper = this.tableSeparators.createMapper(metaData);
        this.mapper.startTable(this.converter, this.alreadyWrite, this.joins);
    }

    @Override
    public void endTable() {
        ComparableDataSetImpl.LOGGER.debug("endTable() - start");
        this.mapper.endTable(this._orderedTableNameMap);
        this.mapper = null;
    }

    @Override
    public void row(final Object[] values) {
        ComparableDataSetImpl.LOGGER.debug("row(values={}) - start", values);
        this.mapper.addRow(values);
    }

    @Override
    public Stream<Map<String, Object>> toMap() {
        return this.toMap(this.param.mapIncludeMetaData());
    }

    @Override
    public Stream<Map<String, Object>> toMap(final boolean includeMetaData) {
        try {
            if (this.param.source() == DataSourceType.none) {
                return Stream.of(Map.of("rowNumber", 0));
            }
            return Arrays.stream(this.getTableNames())
                    .map(tableName -> this.getTable(tableName).toMap(includeMetaData))
                    .flatMap(Collection::stream);
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
        return this.src;
    }

    @Override
    protected ITableIterator createIterator(final boolean reversed) {
        ComparableDataSetImpl.LOGGER.debug("createIterator(reversed={}) - start", reversed);
        return new DefaultTableIterator((ITable[]) (this._orderedTableNameMap.orderedValues().toArray(new ITable[0])));
    }

    @Override
    public ComparableTable getTable(final String tableName) {
        try {
            return (ComparableTable) super.getTable(tableName);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private void executeJoin() throws AmbiguousTableNameException {
        while (!this.joins.isEmpty()) {
            this._orderedTableNameMap = this.orderedTableNameMapExcludeJoins();
            this.joins.stream()
                    .filter(ComparableTableJoin::isExecutable)
                    .toList()
                    .forEach(join -> {
                        ComparableDataSetImpl.LOGGER.debug("startTableJoin(join={}) - start", join);
                        this.joins.remove(join);
                        this.mapper = this.tableSeparators.createMapper(join);
                        this.mapper.startTable(this.converter, this.alreadyWrite, this.joins);
                        join.execute().forEach(this::row);
                        this.endTable();
                    });
        }
    }

    private OrderedTableNameMap orderedTableNameMapExcludeJoins() throws AmbiguousTableNameException {
        final List<String> joinSource = this.joins.stream()
                .filter(ComparableTableJoin::isExecutable)
                .flatMap(it -> Stream.of(it.getCondition().left(), it.getCondition().right()))
                .toList();
        final OrderedTableNameMap excludeJoinSource = super.createTableNameMap();
        for (final String tableName : this._orderedTableNameMap.getTableNames()) {
            if (!joinSource.contains(tableName)) {
                excludeJoinSource.add(tableName, this._orderedTableNameMap.get(tableName));
            }
        }
        return excludeJoinSource;
    }

}
