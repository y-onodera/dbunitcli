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
        , List<ComparableTableJoin> joins) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableTableMappingContext.class);

    public ComparableTableMappingContext(final TableSeparators tableSeparators, final IDataSetConverter converter) {
        this(tableSeparators, converter, new TreeMap<>(), new WrittenTables(), tableSeparators.joins());
    }

    public void open() {
        if (this.converter != null) {
            this.converter.startDataSet();
        }
    }

    public ComparableTableMapper createMapper(final TableMetaDataWithSource metaData) {
        return new ComparableTableMapperBuilder()
                .setConverter(this.converter)
                .setAlreadyWrite(this.alreadyWrite)
                .setJoins(this.joins)
                .setContextShareTableMap(this.tableMap)
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
    private void flushSplits() {
        this.alreadyWrite.takePendingSplits().forEach(pending -> {
            final List<ComparableTable> tables = pending.split();
            tables.forEach(table -> {
                this.tableMap.put(table.getTableName(), table);
                this.alreadyWrite.putSplitTable(table.getTableName(), pending.firstTableName());
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
     * joinで使われずに残った元テーブル（相手のテーブルが無かったもの）のうち、読込時に出力の絞り込みで除くと決めたものを除く
     */
    private void excludeUnusedJoinInputs() {
        this.tableMap.values().removeIf(table -> table.getTableMetaData().isOutputExcluded());
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
                    // joinの出力テーブルには、左右のテーブルに行を入れたタスクが行を入れたものとする
                    final Set<ComparableTableMappingTask> previous = this.alreadyWrite.reading(Stream.concat(
                            this.alreadyWrite.sources(join.getCondition().left()).stream()
                            , this.alreadyWrite.sources(join.getCondition().right()).stream()).toList());
                    final List<ComparableTableJoin> newRemaining = remaining.stream()
                            .filter(it -> !it.equals(join))
                            .toList();
                    final ComparableTableMapper joinMapper = new ComparableTableMapperBuilder()
                            .setConverter(this.converter)
                                        .setContextShareTableMap(this.tableMap)
                            .setAlreadyWrite(this.alreadyWrite)
                            .setJoins(newRemaining)
                            .build(this.tableSeparators.getAddSettingTableMetaData(join));
                    joinMapper.startTable();
                    join.execute().forEach(joinMapper::addRow);
                    joinMapper.endTable();
                    this.flushSplits();
                    this.alreadyWrite.reading(previous);
                    this.processExecutableJoins(newRemaining);
                });
    }

}
