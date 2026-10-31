package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComparableTableMapperSingle implements ComparableTableMapper {

    private final IDataSetConverter converter;
    private final WrittenTables written;
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
    private final List<Object[]> rows;
    private final TableSplitter splitter;
    private final Function<Object[], List<String>> breakKeys;
    private final WrittenTables.SplitState splitState;
    private final boolean hasOutput;
    /**
     * 分割のまとまり（分割後の1つ目のテーブル名。分割しないテーブルはテーブル名）
     */
    private final String groupTableName;
    private AddSettingTableMetaData metaData;
    private boolean startTable;
    private int addCount = 0;
    private int addRowCount = 0;

    public ComparableTableMapperSingle(final AddSettingTableMetaData metaData, final IDataSetConverter converter,
                                       final WrittenTables written, final List<ComparableTableJoin> joins,
                                       final List<ComparableTableMappingTask> sources, final boolean enableRowProcessing) {
        this.baseMetaData = metaData;
        this.splitter = metaData.getTableSplitter();
        this.breakKeys = this.splitter.breakKeyReader(metaData);
        this.groupTableName = this.splitter.tableName(metaData.getTableName(), 0);
        // 行を流しながら分割する場合は、同じテーブルに前のソースが書いた続きから分割する
        this.splitState = enableRowProcessing && this.splitter.isSplit()
                ? written.split(this.groupTableName, this.splitter)
                : WrittenTables.SplitState.start(this.splitter);
        this.metaData = this.splitter.getMetaData(this.baseMetaData, this.splitState.no());
        this.rows = new ArrayList<>();
        this.originalRowIndexes = new ArrayList<>();
        this.converter = converter;
        this.written = written;
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
        this.written.put(this.metaData.getTableName(), this.getAddRowCount());
        this.written.wrote(this.groupTableName, this.sources);
        if (this.splitter.isSplit()) {
            this.written.putSplitTable(this.baseMetaData.getTableName(), this.groupTableName);
            // 行を保持して分割するテーブルの分割後の名前は、分割を確定させるときに記録する
            if (this.enableRowProcessing) {
                this.written.putSplitTable(this.metaData.getTableName(), this.groupTableName);
            }
        }
        if (this.enableRowProcessing && this.startTable) {
            try {
                this.converter.endTable();
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        } else {
            // 行を保持するテーブルは、読込の終わりに同じテーブルに入るすべてのソースの行をまとめて1回だけ確定させる
            // （分割するテーブルは、行を流しながら分割する場合と同じくソースをまたいで、並べ替えてから分割する）。
            // joinへは確定させたときに渡す（ComparableTableMappingContext#flushPendingTables）
            this.written.pendingTable(this.groupTableName, this.splitter)
                        .add(this.baseMetaData, this.baseMetaData.distinct(new AddSettingTableMetaData.Rows(this.rows, this.originalRowIndexes)), this.joins);
        }
    }

    protected void processingStart() {
        try {
            if (this.written.contains(this.metaData.getTableName())) {
                this.converter.reStartTable(this.metaData, this.written.rows(this.metaData.getTableName()));
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
        if (this.splitState.counter().isBreak(this.breakKeys.apply(applySetting), this.getAddRowCount())) {
            this.endTable();
            this.splitState.next();
            this.metaData = this.splitter.getMetaData(this.baseMetaData, this.splitState.no());
            this.processingStart();
            this.addRowCount = 0;
        }
    }

    protected int getAddRowCount() {
        return this.addRowCount + this.written.rows(this.metaData.getTableName());
    }

}
