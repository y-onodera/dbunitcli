package yo.dbunitcli.dataset;

import java.util.*;

/**
 * 行を保持するテーブルの、確定を待っている行。同じテーブルに複数のソースから行が入る場合はソースをまたいでためておき、
 * 読込の終わりにまとめて1回だけ確定させる（分割するテーブルは並べ替えて分割する）
 */
final class PendingTable {

    private final String firstTableName;
    private final TableSplitter splitter;
    /**
     * ソースごとの行（distinctするテーブルはソースの中で重複を除いたもの）
     */
    private final List<AddSettingTableMetaData.Rows> sourceRows = new ArrayList<>();
    private final Set<ComparableTableJoin> joins = new LinkedHashSet<>();
    private AddSettingTableMetaData metaData;

    PendingTable(final String firstTableName, final TableSplitter splitter) {
        this.firstTableName = firstTableName;
        this.splitter = splitter;
    }

    /**
     * 分割後の1つ目のテーブル名（分割のまとまり）。分割しないテーブルはテーブル名
     */
    String firstTableName() {
        return this.firstTableName;
    }

    boolean isSplit() {
        return this.splitter.isSplit();
    }

    /**
     * 1つのソースの行を加える。テーブルは最後に加えたソースの設定で作る
     */
    void add(final AddSettingTableMetaData metaData, final AddSettingTableMetaData.Rows rows, final List<ComparableTableJoin> joins) {
        this.metaData = metaData;
        this.sourceRows.add(rows);
        this.joins.addAll(joins);
    }

    /**
     * 1つ目のテーブルを渡すjoin
     */
    Set<ComparableTableJoin> joins() {
        return this.joins;
    }

    /**
     * ためた行を確定させたテーブル（分割するテーブルは並べ替えて分割した、分割後のテーブルを1つ目から順に。少なくとも1つ）。
     * ためた行はここで手放す
     */
    List<ComparableTable> tables() {
        final AddSettingTableMetaData.Rows targetRows = this.mergeSourceRows();
        this.sourceRows.clear();
        final ComparableTable sorted = new ComparableTable.Builder(this.metaData).setRows(targetRows).build();
        if (!this.isSplit()) {
            return List.of(sorted);
        }
        final List<ComparableTable> result = new ArrayList<>();
        final TableSplitter.Counter counter = this.splitter.counter();
        List<Object[]> targetValues = new ArrayList<>();
        List<Integer> targetRowIndexes = new ArrayList<>();
        for (int i = 0, j = targetRows.size(); i < j; i++) {
            final Object[] row = sorted.getRow(i);
            if (counter.isBreak(this.splitter.breakKeys(this.metaData, row), targetValues.size())) {
                result.add(this.table(result.size(), targetValues, targetRowIndexes));
                targetValues = new ArrayList<>();
                targetRowIndexes = new ArrayList<>();
            }
            targetValues.add(row);
            targetRowIndexes.add(sorted.getOriginalRowIndex(i));
        }
        result.add(this.table(result.size(), targetValues, targetRowIndexes));
        return result;
    }

    /**
     * ソースごとの行を1つにまとめる。ソースの中の重複はソースごとに除いているため、ソースをまたぐ重複だけを除く
     */
    private AddSettingTableMetaData.Rows mergeSourceRows() {
        if (this.sourceRows.size() == 1) {
            return this.sourceRows.getFirst();
        }
        final AddSettingTableMetaData.Rows merged = AddSettingTableMetaData.Rows.concat(this.sourceRows);
        return this.metaData.isNeedDistinct() ? merged.distinct() : merged;
    }

    private ComparableTable table(final int no, final List<Object[]> values, final List<Integer> rowIndexes) {
        return new ComparableTable.Builder(this.splitter.getMetaData(this.metaData, no))
                .setRows(new AddSettingTableMetaData.Rows(values, rowIndexes))
                .build();
    }
}
