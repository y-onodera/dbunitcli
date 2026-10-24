package yo.dbunitcli.application;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableTableDto;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.resource.Workers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * unit=recordの出力単位のパラメータを、読み込んだ行から1行ずつ作って渡す（データセット全体を保持しない）。
 * パラメータはinputParam・tableName・rowNumber（テーブルごとの0始まりの番号）と行の列の値。
 * includeMetaDataでは各行にテーブル全体（ComparableTableDto）とrowを渡すため、行を溜めて読込の終わり（endDataSet）に渡す
 * （同じテーブルの読込が後で再開されうるため、読込を終えるまで溜める）。
 * 並列読込では作業スレッドごとに分け、テーブルは1つの作業スレッドだけが読むため、行番号はテーブル内で読込順になる
 */
public final class RecordConverter implements IDataSetConverter {

    private final Parameter base;

    private final Consumer<Parameter> action;

    private final boolean includeMetaData;

    /**
     * テーブルごとの次の行番号。同じテーブルの読込が分かれても（reStartTable）続きから振る
     */
    private final Map<String, Integer> rowNumbers = new HashMap<>();

    /**
     * includeMetaDataで溜めているテーブル
     */
    private final Map<String, BufferedTable> tables = new LinkedHashMap<>();

    private String tableName;

    private Column[] columns;

    /**
     * @param base   各行のパラメータの元（inputParam）
     * @param action 行ごとのパラメータを受け取る。並列読込では複数スレッドから呼ばれる
     */
    public RecordConverter(final Parameter base, final Consumer<Parameter> action, final boolean includeMetaData) {
        this.base = base;
        this.action = action;
        this.includeMetaData = includeMetaData;
    }

    @Override
    public boolean isExportEmptyTable() {
        return false;
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        this.tableName = metaData.getTableName();
        this.columns = metaData.getColumns();
        if (this.includeMetaData) {
            this.tables.computeIfAbsent(this.tableName, k -> new BufferedTable(metaData, new ArrayList<>()));
        }
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
        try {
            this.startTable(tableMetaData);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 行の値は読み込んだまま渡す（読込後にまとめて渡されるテーブルの空の値はNO_VALUEになるためnullに戻す）
     */
    @Override
    public void row(final Object[] values) {
        final Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            row.put(this.columns[i].getColumnName(), values[i] == ITable.NO_VALUE ? null : values[i]);
        }
        if (this.includeMetaData) {
            this.tables.get(this.tableName).rows().add(row);
            return;
        }
        final int rowNumber = this.rowNumbers.merge(this.tableName, 1, Integer::sum) - 1;
        // 列の値を後に入れる（tableNameという列があれば列の値を優先する）
        this.action.accept(this.base.withRowNumber(rowNumber).add("tableName", this.tableName).addAll(row));
    }

    @Override
    public void endDataSet() {
        this.tables.values().forEach(table -> {
            final ComparableTableDto dto = new ComparableTableDto(table.metaData(), table.rows());
            for (int i = 0; i < table.rows().size(); i++) {
                this.action.accept(this.base.withRowNumber(i).addAll(dto).add("row", table.rows().get(i)));
            }
        });
        this.tables.clear();
    }

    @Override
    public void endTable() {
        // 行ごとに渡し終えている（includeMetaDataはendDataSetで渡す）
    }

    @Override
    public IDataSetConverter split() {
        return this;
    }

    @Override
    public boolean isParallelizable() {
        return true;
    }

    @Override
    public IDataSetConverter splitForParallel(final Workers.WorkerResources resources) {
        return new RecordConverter(this.base, this.action, this.includeMetaData);
    }

    private record BufferedTable(ITableMetaData metaData, List<Map<String, Object>> rows) {
    }
}
