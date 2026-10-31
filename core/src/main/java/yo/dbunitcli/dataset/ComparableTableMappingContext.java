package yo.dbunitcli.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.*;
import java.util.stream.Stream;

public record ComparableTableMappingContext(TableSeparators tableSeparators
        , IDataSetConverter converter
        , TreeMap<String, ComparableTable> tableMap
        , WrittenTables alreadyWrite
        , List<ComparableTableJoin> joins
        , List<ComparableTableMappingTask> chain
        , boolean chainRun
        , Listener listener) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableTableMappingContext.class);

    public ComparableTableMappingContext(final TableSeparators tableSeparators, final IDataSetConverter converter) {
        this(tableSeparators, converter, new TreeMap<>(), new WrittenTables(), tableSeparators.joins(), new ArrayList<>(), false, Listener.NONE);
    }

    public ComparableTableMappingContext addChain(final List<ComparableTableMappingTask> chain) {
        return new ComparableTableMappingContext(this.tableSeparators
                , this.converter
                , this.tableMap
                , this.alreadyWrite
                , this.joins
                , chain
                , false
                , this.listener);
    }

    public void open() {
        if (this.converter != null) {
            this.converter.startDataSet();
        }
    }

    public ComparableTableMapper createMapper(final TableMetaDataWithSource metaData) {
        return new ComparableTableMapperBuilder()
                .setConverter(this.converter)
                .setTableSeparators(this.tableSeparators)
                .setAlreadyWrite(this.alreadyWrite)
                .setJoins(this.joins)
                .setChain(this.chain)
                .setChainRun(this.chainRun)
                .setContextShareTableMap(this.tableMap)
                .setListener(this.listener)
                .build(this.tableSeparators.getAddSettingTableMetaData(metaData));
    }

    public TreeMap<String, ComparableTable> close() {
        this.executeJoin();
        if (this.converter != null) {
            this.tableMap.values().forEach(this.converter::convert);
            this.converter.endDataSet();
            return new TreeMap<>();
        }
        return this.tableMap;
    }

    public Stream<AddSettingTableMetaData> getAddSettingTableMetaData(final ComparableTableJoin join) {
        return this.tableSeparators.getAddSettingTableMetaData(join);
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
                    final ComparableTableMapper joinMapper = new ComparableTableMapperBuilder()
                            .setConverter(this.converter)
                            .setTableSeparators(this.tableSeparators)
                            .setContextShareTableMap(this.tableMap)
                            .setAlreadyWrite(this.alreadyWrite)
                            .setJoins(newRemaining)
                            .setChain(this.chain)
                            .setChainRun(this.chainRun)
                            .setListener(this.listener)
                            .build(this.getAddSettingTableMetaData(join));
                    joinMapper.startTable();
                    join.execute().forEach(joinMapper::addRow);
                    joinMapper.endTable();
                    this.listener.joined(join);
                    this.processExecutableJoins(newRemaining);
                });
    }

    /**
     * 読込の途中経過の通知を受ける
     */
    public interface Listener {

        Listener NONE = new Listener() {
        };

        /**
         * テーブルを読込の終わりにまとめて出力するため、共有のtableMapに入れた（入れ直した）
         */
        default void held(final String tableName) {
            // 既定では何もしない
        }

        /**
         * joinを1つ実行し終えた。そのjoinの出力テーブルの通知（held・converterのstartTable）は、この前に済んでいる
         */
        default void joined(final ComparableTableJoin join) {
            // 既定では何もしない
        }
    }

}
