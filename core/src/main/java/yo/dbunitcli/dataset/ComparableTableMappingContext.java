package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.*;
import java.util.stream.Stream;

/**
 * タスクごとの独立した状態管理クラス
 * 並列実行時に各タスクが独自のコンテキストを持つことで、スレッドセーフな処理を実現
 */
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
        this.tableSeparators = tableSeparators;
        this.converter = converter;
        this.tableMap = new TreeMap<>();
        this.alreadyWrite = new HashMap<>();
        this.joins = this.tableSeparators.joins();
        this.chain = new ArrayList<>();
        this.chainRun = false;
        if (this.converter != null) {
            try {
                this.converter.startDataSet();
            } catch (final DataSetException e) {
                throw new RuntimeException(e);
            }
        }
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

    public void startTable(final TableMetaDataWithSource metaData) {
        LOGGER.debug("startTable(metaData={}) - start", metaData);
        this.currentMapper = this.tableSeparators.createMapper(metaData);
        this.currentMapper.startTable(this.converter, this.alreadyWrite, this.joins, this.chain, this.chainRun);
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
        while (!this.joins.isEmpty()) {
            this.joins.stream()
                    .filter(ComparableTableJoin::isExecutable)
                    .flatMap(it -> Stream.of(it.getCondition().left(), it.getCondition().right()))
                    .forEach(this.tableMap::remove);
            this.joins.stream()
                    .filter(ComparableTableJoin::isExecutable)
                    .toList()
                    .forEach(join -> {
                        LOGGER.debug("startTableJoin(join={}) - start", join);
                        this.joins.remove(join);
                        this.currentMapper = this.tableSeparators.createMapper(join);
                        this.currentMapper.startTable(this.converter, this.alreadyWrite, this.joins, this.chain, this.chainRun);
                        join.execute().forEach(this::row);
                        this.currentMapper.endTable(this.tableMap);
                    });
        }
    }

}
