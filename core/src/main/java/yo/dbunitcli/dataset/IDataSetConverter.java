package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.resource.Workers;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public interface IDataSetConverter extends IDataSetConsumer, AutoCloseable {

    boolean isExportEmptyTable();

    default void startDataSet() {
        // nothing default
    }

    default void endDataSet() {
        // nothing default
    }

    /**
     * 出力に使った資源（DB接続など）を解放する。作った側が出力の完了後に呼ぶ
     */
    @Override
    default void close() {
        // nothing default
    }

    void reStartTable(AddSettingTableMetaData tableMetaData, Integer writeRows);

    default void convert(final ITable aTable) {
        if (!this.isExportEmptyTable() && aTable.getRowCount() == 0) {
            return;
        }
        try {
            this.startTable(aTable.getTableMetaData());
            IntStream.range(0, aTable.getRowCount()).forEach(i -> this.convertRow(aTable, i));
            this.endTable();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default void convertRow(final ITable aTable, final int rowNum) {
        try {
            this.convertRow(Arrays.stream(aTable.getTableMetaData().getColumns())
                    .map(column -> this.getValue(aTable, rowNum, column))
                    .toArray(Object[]::new));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default void convertRow(final Object[] row) {
        try {
            this.row(row);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default Object getValue(final ITable aTable, final int i, final Column column) {
        try {
            return aTable.getValue(i, column.getColumnName());
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    default File getDir() {
        return null;
    }

    default boolean isSplittable() {
        return true;
    }

    IDataSetConverter split();

    /**
     * splitForParallel()した複数のインスタンスを別スレッドで同時に使えるか（出力先が互いに独立しているか）
     */
    default boolean isParallelizable() {
        return false;
    }

    /**
     * 別スレッドで使うインスタンスを作る。startDataSet()は呼び出し側が行う。
     * スレッド間で共有できない資源（DB接続など）はresourcesから取得する（スレッドの処理終了時に閉じられる）
     */
    default IDataSetConverter splitForParallel(final Workers.WorkerResources resources) {
        return this.split();
    }

    /**
     * このテーブルの行を使うか。falseならproducerは行の読込を省く（{@link ComparableTableMapper#hasOutput()}）
     *
     * @param joins このテーブルに関係するjoin
     */
    default boolean isRowRequired(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
        return true;
    }

    default boolean isEnableRowProcessing(final AddSettingTableMetaData metaData, final List<ComparableTableJoin> joins) {
        return metaData.getOrderColumns().length == 0
                && !metaData.isNeedDistinct()
                && joins.isEmpty();
    }
}
