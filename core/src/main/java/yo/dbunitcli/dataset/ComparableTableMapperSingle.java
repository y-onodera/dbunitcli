package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComparableTableMapperSingle implements ComparableTableMapper {

    private final IDataSetConverter converter;
    private final WrittenTables alreadyWrite;
    private final TreeMap<String, ComparableTable> contextShareTableMap;
    /**
     * このテーブルに関係するjoin
     */
    private final List<ComparableTableJoin> joins;
    /**
     * 書いたテーブルに行を入れたと記録するタスク
     */
    private final List<ComparableTableMappingTask> sources;
    private final boolean enableRowProcessing;
    private final AddSettingTableMetaData baseMetaData;
    /**
     * 保持した行の、ソースでの行番号。行を除くまでは記録しない（{@link AddSettingTableMetaData.Rows#originalRowIndexes()}）
     */
    private final List<Integer> originalRowIndexes;
    private final Collection<Object[]> rows;
    private final TableSplitter splitter;
    private final WrittenTables.SplitState splitState;
    private final boolean hasOutput;
    /**
     * 分割後の1つ目のテーブル名（分割のまとまり）
     */
    private final String firstTableName;
    private AddSettingTableMetaData metaData;
    private boolean startTable;
    private int addCount = 0;
    private int addRowCount = 0;

    public ComparableTableMapperSingle(final AddSettingTableMetaData metaData, final IDataSetConverter converter,
                                       final TreeMap<String, ComparableTable> contextShareTableMap,
                                       final WrittenTables alreadyWrite, final List<ComparableTableJoin> joins,
                                       final List<ComparableTableMappingTask> sources, final boolean enableRowProcessing) {
        this.baseMetaData = metaData;
        this.splitter = metaData.getTableSplitter();
        this.firstTableName = this.splitter.tableName(metaData.getTableName(), 0);
        // 行を流しながら分割する場合は、同じテーブルに前のソースが書いた続きから分割する
        this.splitState = enableRowProcessing && this.splitter.isSplit()
                ? alreadyWrite.split(this.firstTableName, this.splitter)
                : WrittenTables.SplitState.start(this.splitter);
        this.metaData = this.splitter.getMetaData(this.baseMetaData, this.splitState.no());
        this.rows = new ArrayList<>();
        this.originalRowIndexes = new ArrayList<>();
        this.converter = converter;
        this.contextShareTableMap = contextShareTableMap;
        this.alreadyWrite = alreadyWrite;
        this.joins = joins.stream().filter(join -> join.hasRelation(metaData)).collect(Collectors.toList());
        this.sources = sources;
        this.enableRowProcessing = enableRowProcessing;
        this.hasOutput = converter == null || converter.isRowRequired(metaData, this.joins);
    }

    @Override
    public void startTable() {
        if (this.enableRowProcessing && this.converter.isExportEmptyTable()) {
            this.processingStart();
            this.startTable = true;
        }
    }

    @Override
    public void addRow(final Object[] values) {
        if (this.metaData.isNeedDistinct()) {
            // distinctは保持した行に設定を適用するときに行を除き、行番号を残す（AddSettingTableMetaData#distinct）
            this.rows.add(values);
            this.addCount++;
        } else {
            this.addValue(this.metaData.applySetting(values));
        }
    }

    @Override
    public boolean hasOutput() {
        return this.hasOutput;
    }

    @Override
    public void endTable() {
        this.alreadyWrite.put(this.metaData.getTableName(), this.getAddRowCount());
        this.alreadyWrite.wrote(this.splitter.isSplit() ? this.firstTableName : this.metaData.getTableName(), this.sources);
        if (this.enableRowProcessing && this.splitter.isSplit()) {
            this.alreadyWrite.putSplitTable(this.metaData.getTableName(), this.firstTableName);
        }
        if (this.enableRowProcessing && this.startTable) {
            try {
                this.converter.endTable();
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        } else {
            if (this.splitter.isSplit()) {
                this.addPendingSplit();
            } else if (this.contextShareTableMap.containsKey(this.metaData.getTableName())) {
                final ComparableTable other = this.contextShareTableMap.get(this.metaData.getTableName());
                final AddSettingTableMetaData.Rows add = other.getRows().add(this.metaData.distinct(this.rows,
                                                                                                    this.originalRowIndexes));
                this.hold(this.metaData.getTableName(), this.metaData.isNeedDistinct()
                        ? new ComparableTable.Builder(this.metaData).setRows(add.distinct()).build()
                        : new ComparableTable.Builder(this.metaData).setRows(add).build());
            } else {
                this.hold(this.metaData.getTableName(), this.lazyProcess());
            }
            // 分割するテーブルは、分割を確定させるときにjoinへ渡す（ComparableTableMappingContext#flushSplits）
            if (!this.splitter.isSplit()) {
                this.joins.forEach(it -> it.setIfRelated(this.contextShareTableMap.get(this.metaData.getTableName())));
            }
        }
    }

    /**
     * 保持した行を、分割を待つ行に加える。分割は読込の終わりに、同じテーブルに入るすべてのソースの行をまとめて1回だけ行う
     * （行を流しながら分割する場合と同じく、ソースをまたいで分割する）
     */
    private void addPendingSplit() {
        this.alreadyWrite.pendingSplit(this.firstTableName, this.splitter)
                         .add(this.baseMetaData, this.baseMetaData.distinct(this.rows, this.originalRowIndexes), this.joins);
    }

    private void hold(final String tableName, final ComparableTable table) {
        this.contextShareTableMap.put(tableName, table);
    }

    protected void processingStart() {
        try {
            if (this.alreadyWrite.contains(this.metaData.getTableName())) {
                this.converter.reStartTable(this.metaData, this.alreadyWrite.rows(this.metaData.getTableName()));
            } else {
                this.converter.startTable(this.metaData);
            }
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void addValue(final Object[] applySetting) {
        try {
            if (applySetting != null) {
                if (this.enableRowProcessing) {
                    if (!this.converter.isExportEmptyTable() && !this.startTable) {
                        this.processingStart();
                        this.startTable = true;
                    }
                    this.splitTable(applySetting);
                    this.converter.row(applySetting);
                } else {
                    this.rows.add(applySetting);
                    this.recordOriginalRowIndex();
                }
                this.addRowCount++;
            }
            this.addCount++;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 保持した行の、ソースでの行番号（compareの$ROW_ORIGINAL）を記録する。filterで行を除くまでは行の位置と同じため記録せず、
     * 初めて除いたときにそれまでの行番号を埋める
     */
    private void recordOriginalRowIndex() {
        final int position = this.rows.size() - 1;
        if (this.originalRowIndexes.isEmpty()) {
            if (this.addCount == position) {
                return;
            }
            IntStream.range(0, position).forEach(this.originalRowIndexes::add);
        }
        this.originalRowIndexes.add(this.addCount);
    }

    protected void splitTable(final Object[] applySetting) throws DataSetException {
        if (!this.splitter.isSplit()) {
            return;
        }
        if (this.splitState.counter().isBreak(this.splitter.breakKeys(this.metaData, applySetting), this.getAddRowCount())) {
            this.endTable();
            this.splitState.next();
            this.metaData = this.splitter.getMetaData(this.baseMetaData, this.splitState.no());
            this.processingStart();
            this.addRowCount = 0;
        }
    }

    protected void add(final ComparableTable other) {
        IntStream.range(0, other.getRowCount())
                 .forEach(rowNum -> this.addValue(
                         Arrays.stream(this.metaData.getColumns())
                               .map(column -> other.getValue(
                                       rowNum,
                                       column.getColumnName()))
                               .toArray(Object[]::new))
                 );
    }

    protected ComparableTable lazyProcess() {
        return new ComparableTable.Builder(this.metaData)
                .setRows(this.metaData.distinct(this.rows, this.originalRowIndexes))
                .build();
    }

    protected int getAddRowCount() {
        return this.addRowCount + this.alreadyWrite.rows(this.metaData.getTableName());
    }

}
