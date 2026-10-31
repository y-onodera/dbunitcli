package yo.dbunitcli.dataset;

import java.util.*;

/**
 * 行を保持して分割するテーブルの、分割を待っている行。同じテーブルに複数のソースから行が入る場合は
 * ソースをまたいでためておき、読込の終わりにまとめて1回だけ並べ替えて分割する
 */
final class PendingSplit {

    private final String firstTableName;
    private final TableSplitter splitter;
    /**
     * ソースごとの行（distinctするテーブルはソースの中で重複を除いたもの）
     */
    private final List<AddSettingTableMetaData.Rows> sourceRows = new ArrayList<>();
    private final Set<ComparableTableJoin> joins = new LinkedHashSet<>();
    private AddSettingTableMetaData metaData;

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
     * 1つのソースの行を加える。分割後のテーブルは最後に加えたソースの設定で作る
     */
    void add(final AddSettingTableMetaData metaData, final AddSettingTableMetaData.Rows rows, final List<ComparableTableJoin> joins) {
        this.metaData = metaData;
        this.sourceRows.add(rows);
        this.joins.addAll(joins);
    }

    /**
     * 分割後の1つ目のテーブルを渡すjoin
     */
    Set<ComparableTableJoin> joins() {
        return this.joins;
    }

    /**
     * ためた行を並べ替えて分割した、分割後のテーブル（1つ目から順に、少なくとも1つ）。ためた行はここで手放す
     */
    List<ComparableTable> split() {
        final AddSettingTableMetaData.Rows targetRows = this.mergeSourceRows();
        this.sourceRows.clear();
        final ComparableTable sorted = new ComparableTable.Builder(this.metaData).setRows(targetRows).build();
        final List<ComparableTable> result = new ArrayList<>();
        final TableSplitter.Counter counter = this.splitter.counter();
        List<Object[]> targetValues = new ArrayList<>();
        List<Integer> targetRowIndexes = new ArrayList<>();
        final boolean hasRowIndexes = targetRows.filteredRowIndexes().size() == targetRows.size();
        for (int i = 0, j = targetRows.size(); i < j; i++) {
            // 並べ替え後のi行目と、その行のソースでの行番号は、並べ替え前の位置で対応させる
            final int before = sorted.getIndexBeforeSort(i);
            final Object[] row = targetRows.get(before);
            if (counter.isBreak(this.splitter.breakKeys(this.metaData, row), targetValues.size())) {
                result.add(this.table(result.size(), targetValues, targetRowIndexes));
                targetValues = new ArrayList<>();
                targetRowIndexes = new ArrayList<>();
            }
            targetValues.add(row);
            if (hasRowIndexes) {
                targetRowIndexes.add(targetRows.filteredRowIndexes().get(before));
            }
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
        final int size = this.sourceRows.stream().mapToInt(AddSettingTableMetaData.Rows::size).sum();
        final List<Object[]> rows = new ArrayList<>(size);
        final List<Integer> filteredRowIndexes = new ArrayList<>(size);
        this.sourceRows.forEach(it -> {
            rows.addAll(it.rows());
            filteredRowIndexes.addAll(it.filteredRowIndexes());
        });
        final AddSettingTableMetaData.Rows merged = new AddSettingTableMetaData.Rows(rows, filteredRowIndexes);
        return this.metaData.isNeedDistinct() ? merged.distinct() : merged;
    }

    private ComparableTable table(final int no, final List<Object[]> values, final List<Integer> rowIndexes) {
        return new ComparableTable.Builder(this.splitter.getMetaData(this.metaData, no))
                .setRows(new AddSettingTableMetaData.Rows(values, rowIndexes))
                .build();
    }
}
