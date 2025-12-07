package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.*;
import java.util.stream.Stream;

public class ComparableTableMappingContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableTableMappingContext.class);

    private final TableSeparators tableSeparators;
    private final IDataSetConverter converter;
    private final TreeMap<String, ComparableTable> tableMap;
    private final Map<String, Integer> alreadyWrite;
    private final List<ComparableTableJoin> joins;
    private final List<ComparableTableMappingTask> chain;
    private final boolean chainRun;
    private ComparableTableMapper currentMapper;

    public ComparableTableMappingContext(final TableSeparators tableSeparators, final IDataSetConverter converter) {
        this(tableSeparators, converter, new TreeMap<>(), new HashMap<>(), tableSeparators.joins(), new ArrayList<>(), false);
    }

    public ComparableTableMappingContext(final TableSeparators tableSeparators, final IDataSetConverter converter, final TreeMap<String, ComparableTable> tableMap, final Map<String, Integer> alreadyWrite, final List<ComparableTableJoin> joins, final List<ComparableTableMappingTask> chain, final boolean chainRun) {
        this.tableSeparators = tableSeparators;
        this.converter = converter;
        this.tableMap = tableMap;
        this.alreadyWrite = alreadyWrite;
        this.joins = joins;
        this.chain = chain;
        this.chainRun = chainRun;
    }

    public ComparableTableMappingContext addChain(final List<ComparableTableMappingTask> chain) {
        return new ComparableTableMappingContext(this.tableSeparators
                , this.converter
                , this.tableMap
                , this.alreadyWrite
                , this.joins
                , chain
                , false);
    }

    public void open() {
        if (this.converter != null) {
            try {
                this.converter.startDataSet();
            } catch (final DataSetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void startTable(final TableMetaDataWithSource metaData) {
        LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.currentMapper = new ComparableTableMapperBuilder()
                .setConverter(this.converter)
                .setAlreadyWrite(this.alreadyWrite)
                .setJoins(this.joins)
                .setChain(this.chain)
                .setChainRun(this.chainRun)
                .build(this.tableSeparators.getAddSettingTableMetaData(metaData));
        this.currentMapper.startTable();
    }

    public void row(final Object[] values) {
        LOGGER.debug("row(values={}) - start", values);
        this.currentMapper.addRow(values);
    }

    public void endTable() {
        LOGGER.debug("endTable() - start");
        this.currentMapper.endTable(this.tableMap);
    }

    public TreeMap<String, ComparableTable> close() {
        this.executeJoin();
        if (this.converter != null) {
            this.tableMap.values().forEach(this.converter::convert);
            try {
                this.converter.endDataSet();
            } catch (final DataSetException e) {
                throw new RuntimeException(e);
            }
            return new TreeMap<>();
        }
        return this.tableMap;
    }

    private void executeJoin() {
        if (!this.joins.isEmpty()) {
            this.joins.stream()
                    .filter(ComparableTableJoin::isExecutable)
                    .flatMap(it -> Stream.of(it.getCondition().left(), it.getCondition().right()))
                    .forEach(this.tableMap::remove);
            this.processExecutableJoins(new ArrayList<>(this.joins));
        }
    }

    private void processExecutableJoins(final List<ComparableTableJoin> remaining) {
        remaining.stream()
                .filter(ComparableTableJoin::isExecutable)
                .findFirst()
                .ifPresent(join -> {
                    LOGGER.debug("startTableJoin(join={}) - start", join);
                    final List<ComparableTableJoin> newRemaining = remaining.stream()
                            .filter(it -> !it.equals(join))
                            .toList();
                    this.currentMapper = new ComparableTableMapperBuilder()
                            .setConverter(this.converter)
                            .setAlreadyWrite(this.alreadyWrite)
                            .setJoins(newRemaining)
                            .setChain(this.chain)
                            .setChainRun(this.chainRun)
                            .build(this.tableSeparators.getAddSettingTableMetaData(join));
                    this.currentMapper.startTable();
                    join.execute().forEach(this::row);
                    this.currentMapper.endTable(this.tableMap);
                    this.processExecutableJoins(newRemaining);
                });
    }
}
