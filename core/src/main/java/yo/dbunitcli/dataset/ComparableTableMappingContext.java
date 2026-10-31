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
        this.flushSplits();
        this.executeJoin();
        this.excludeUnusedJoinInputs();
        if (this.converter != null) {
            this.tableMap.values().forEach(this.converter::convert);
            this.converter.endDataSet();
            return new TreeMap<>();
        }
        return this.tableMap;
    }

    /**
     * 分割を待っている行（行を保持して分割するテーブル）を、まとめて1回だけ並べ替えて分割し、分割後のテーブルを確定させる。
     * 分割後の1つ目のテーブルを関係するjoinへ渡す
     */
    void flushSplits() {
        this.alreadyWrite.takePendingSplits().forEach(pending -> {
            final List<ComparableTable> tables = pending.split();
            tables.forEach(table -> {
                this.tableMap.put(table.getTableName(), table);
                this.alreadyWrite.putSplitTable(table.getTableName(), pending.firstTableName());
                this.listener.held(table.getTableName());
            });
            pending.joins().forEach(join -> join.setIfRelated(tables.getFirst()));
        });
    }

    private void executeJoin() {
        if (!this.joins.isEmpty()) {
            this.processExecutableJoins(new ArrayList<>(this.joins));
        }
    }

    /**
     * joinで使われずに残った元テーブル（相手のテーブルが無かったもの）のうち、出力の絞り込みで除かれるものを除く
     */
    private void excludeUnusedJoinInputs() {
        this.tableMap.values().removeIf(table -> !this.tableSeparators.filter().test(table.getTableMetaData())
                && this.joins.stream().anyMatch(join -> join.hasRelation(table.getTableMetaData())));
    }

    private void processExecutableJoins(final List<ComparableTableJoin> remaining) {
        remaining.stream()
                .filter(ComparableTableJoin::isExecutable)
                .findFirst()
                .ifPresent(join -> {
                    LOGGER.debug("startTableJoin(join={}) - start", join);
                    // 元テーブルはjoinの結果に置き換える（joinの結果を使うjoinでは、使われたjoinの結果も置き換える）
                    this.tableMap.remove(join.getCondition().left());
                    this.tableMap.remove(join.getCondition().right());
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
                            .build(this.tableSeparators.getAddSettingTableMetaData(join));
                    joinMapper.startTable();
                    join.execute().forEach(joinMapper::addRow);
                    joinMapper.endTable();
                    this.flushSplits();
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
         * テーブルを読込の終わりにまとめて出力するため、共有のtableMapに入れた（入れ直した）。
         * 行を保持して分割するテーブルは、ソースを読んだ時点では分割後の1つ目のテーブル名で通知し、
         * 分割を確定させたとき（{@link #flushSplits()}）に分割後のテーブルごとに通知する
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
