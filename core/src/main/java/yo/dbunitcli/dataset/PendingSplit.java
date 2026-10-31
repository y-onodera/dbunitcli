package yo.dbunitcli.dataset;

import java.util.*;

/**
 * 行を保持して分割するテーブルの、分割を待っている行。同じテーブルに複数のソースから行が入る場合は
 * ソースをまたいでためておき、読込の終わりにまとめて1回だけ並べ替えて分割する
 */
final class PendingSplit {

    private final String firstTableName;
    private final TableSplitter splitter;
    private final List<Object[]> rows = new ArrayList<>();
    private final List<Integer> filteredRowIndexes = new ArrayList<>();
    private final Set<ComparableTableJoin> joins = new LinkedHashSet<>();
    private AddSettingTableMetaData metaData;
    /**
     * 行を持たず件数だけを数えたソースの行数（{@link IDataSetConverter#isRowCountOnly}）
     */
    private int countOnlyRows;
    private int sources;

    PendingSplit(final String firstTableName, final TableSplitter splitter) {
        this.firstTableName = firstTableName;
        this.splitter = splitter;
    }

    /**
     * 分割後の1つ目のテーブル名（分割のまとまり）
     */
    String firstTableName() {
        return this.firstTableName;
    }

    /**
     * 1つのソースの行（distinctするテーブルはソースの中で重複を除いたもの）を加える。分割後のテーブルは最後に加えたソースの設定で作る
     */
    void add(final AddSettingTableMetaData metaData, final AddSettingTableMetaData.Rows rows, final List<ComparableTableJoin> joins) {
        this.rows.addAll(rows.rows());
        this.filteredRowIndexes.addAll(rows.filteredRowIndexes());
        this.addSource(metaData, joins);
    }

    /**
     * 1つのソースの行数だけを加える（行の値は使わない）
     */
    void addCount(final AddSettingTableMetaData metaData, final int rowCount, final List<ComparableTableJoin> joins) {
        this.countOnlyRows += rowCount;
        this.addSource(metaData, joins);
    }

    private void addSource(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
        this.metaData = metaData;
        this.joins.addAll(joins);
        this.sources++;
    }

    /**
     * 分割後の1つ目のテーブルを渡すjoin
     */
    Set<ComparableTableJoin> joins() {
        return this.joins;
    }

    /**
     * ためた行を並べ替えて分割した、分割後のテーブル（1つ目から順に、少なくとも1つ）
     */
    List<ComparableTable> split() {
        final AddSettingTableMetaData.Rows merged = new AddSettingTableMetaData.Rows(this.rows, this.filteredRowIndexes);
        // ソースの中の重複はソースごとに除いているため、ソースをまたぐ重複だけを除く
        final AddSettingTableMetaData.Rows targetRows = this.metaData.isNeedDistinct() && this.sources > 1 ? merged.distinct() : merged;
        final ComparableTable sorted = new ComparableTable.Builder(this.metaData).setRows(targetRows).build();
        final List<ComparableTable> result = new ArrayList<>();
        final TableSplitter.Counter counter = this.splitter.counter();
        List<Object[]> targetValues = new ArrayList<>();
        List<Integer> targetRowIndexes = new ArrayList<>();
        for (int i = 0, j = targetRows.size(); i < j; i++) {
            final Object[] row = sorted.getRow(i);
            if (counter.isBreak(this.breakKeys(row), targetValues.size())) {
                result.add(this.table(result.size(), targetValues, targetRowIndexes));
                targetValues = new ArrayList<>();
                targetRowIndexes = new ArrayList<>();
            }
            targetValues.add(row);
            if (this.metaData.hasRowFilter()) {
                targetRowIndexes.add(targetRows.filteredRowIndexes().get(i));
            }
        }
        // 件数だけを数えたソースの分は、行の無い分割後のテーブルを件数どおりに作る
        int targetCount = targetValues.size();
        for (int i = 0; i < this.countOnlyRows; i++) {
            if (counter.isBreak(List.of(), targetCount)) {
                result.add(this.table(result.size(), targetValues, targetRowIndexes));
                targetValues = new ArrayList<>();
                targetRowIndexes = new ArrayList<>();
                targetCount = 0;
            }
            targetCount++;
        }
        result.add(this.table(result.size(), targetValues, targetRowIndexes));
        return result;
    }

    private ComparableTable table(final int no, final List<Object[]> values, final List<Integer> rowIndexes) {
        return new ComparableTable.Builder(this.splitter.getMetaData(this.metaData, no))
                .setRows(new AddSettingTableMetaData.Rows(values, rowIndexes))
                .build();
    }

    private List<String> breakKeys(final Object[] row) {
        return this.splitter.breakKeys().isEmpty() ? List.of() : this.metaData.getBreakKeys(row);
    }
}
