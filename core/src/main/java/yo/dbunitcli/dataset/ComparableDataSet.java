package yo.dbunitcli.dataset;

import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComparableDataSet extends AbstractDataSet implements ComparableDataSetConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableDataSet.class);
    private final TableSeparators tableSeparators;
    private final ComparableDataSetParam param;
    private final IDataSetConverter converter;
    private final Map<String, Integer> alreadyWrite;
    private final List<ComparableTableJoin> joins;
    private final String src;
    private ComparableTableMapper mapper;

    public ComparableDataSet(final ComparableDataSetProducer producer) {
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
        ComparableDataSet.LOGGER.debug("startDataSet() - start");
        this._orderedTableNameMap = super.createTableNameMap();
        if (this.converter != null) {
            this.converter.startDataSet();
        }
    }

    public void startTable(final TableMetaDataWithSource metaData) {
        ComparableDataSet.LOGGER.debug("startTable(metaData={}) - start", metaData);
        if (this.converter != null) {
            this.converter.startTableFromSource(metaData);
        }
        this.mapper = this.tableSeparators.createMapper(metaData);
        this.mapper.startTable(this.converter, this.alreadyWrite, this.joins);
    }

    @Override
    public void row(final Object[] values) {
        ComparableDataSet.LOGGER.debug("row(values={}) - start", values);
        this.mapper.addRow(values);
    }

    @Override
    public void endTable() {
        ComparableDataSet.LOGGER.debug("endTable() - start");
        this.mapper.endTable(this._orderedTableNameMap);
        this.mapper = null;
    }

    @Override
    public void endDataSet() throws DataSetException {
        ComparableDataSet.LOGGER.debug("endDataSet() - start");
        this.executeJoin();
        if (this.converter != null) {
            final ITableIterator itr = this.createIterator(false);
            while (itr.next()) {
                this.converter.convert(itr.getTable());
            }
            this.converter.endDataSet();
        }
        ComparableDataSet.LOGGER.debug("endDataSet() - the final tableMap is: " + this._orderedTableNameMap);
    }

    public Stream<Map<String, Object>> toMap() {
        return this.toMap(this.param.mapIncludeMetaData());
    }

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

    public boolean contains(final String tableName) {
        return this._orderedTableNameMap.containsTable(tableName);
    }

    public String getSrc() {
        return this.src;
    }

    @Override
    protected ITableIterator createIterator(final boolean reversed) {
        ComparableDataSet.LOGGER.debug("createIterator(reversed={}) - start", reversed);
        return new DefaultTableIterator((ComparableTable[]) (this._orderedTableNameMap.orderedValues().toArray(new ComparableTable[0])));
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
                        ComparableDataSet.LOGGER.debug("startTableJoin(join={}) - start", join);
                        this.joins.remove(join);
                        if (this.converter != null) {
                            this.converter.startTableFromSource(join.joinMetaData());
                        }
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
