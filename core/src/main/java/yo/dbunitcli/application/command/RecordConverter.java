package yo.dbunitcli.application.command;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.ComparableTableDto;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.ParallelTableMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * unit=recordの出力単位のパラメータを、読み込んだ行から1行ずつ作って渡す（データセット全体を保持しない）。
 * パラメータはinputParam・tableName・rowNumber（テーブルごとの0始まりの番号）と行の列の値。
 * includeMetaDataでは各行にテーブル全体（ComparableTableDto）とrowを渡すため、テーブルの行を溜めて読込の終わり（endDataSet）に渡す。
 * 並列読込では作業スレッドごとに分け、テーブルは1つの作業スレッドだけが読むため、行番号はテーブル内で読込順になる
 */
final class RecordConverter implements IDataSetConverter {

    private final Parameter base;

    private final Consumer<Parameter> action;

    private final boolean includeMetaData;

    /**
     * includeMetaDataで溜めているテーブルごとのメタデータと行
     */
    private final Map<String, ITableMetaData> metaData = new LinkedHashMap<>();

    private final Map<String, List<Map<String, Object>>> rows = new LinkedHashMap<>();

    /**
     * テーブルごとの次の行番号。同じテーブルの読込が分かれても（reStartTable）続きから振る
     */
    private final Map<String, Integer> rowNumbers = new HashMap<>();

    private String tableName;

    private Column[] columns;

    RecordConverter(final Parameter base, final Consumer<Parameter> action, final boolean includeMetaData) {
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
            this.metaData.put(this.tableName, metaData);
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

    @Override
    public void row(final Object[] values) {
        final Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            row.put(this.columns[i].getColumnName(), values[i]);
        }
        if (this.includeMetaData) {
            this.rows.computeIfAbsent(this.tableName, k -> new ArrayList<>()).add(row);
            return;
        }
        final int rowNumber = this.rowNumbers.merge(this.tableName, 1, Integer::sum) - 1;
        // 列の値を後に入れる（tableNameという列があれば列の値を優先する）
        this.action.accept(this.base.withRowNumber(rowNumber).add("tableName", this.tableName).addAll(row));
    }

    @Override
    public void endDataSet() {
        this.rows.forEach((name, tableRows) -> {
            final ComparableTableDto table = new ComparableTableDto(this.metaData.get(name), tableRows);
            for (int i = 0; i < tableRows.size(); i++) {
                this.action.accept(this.base.withRowNumber(i).addAll(table).add("row", tableRows.get(i)));
            }
        });
        this.rows.clear();
        this.metaData.clear();
    }

    /**
     * 並び替え・重複除去・joinのテーブルは読込後にまとめて渡される。行の値は読み込んだまま渡す（空の値をNO_VALUEに変えない）
     */
    @Override
    public void convertRow(final ITable aTable, final int rowNum) {
        if (aTable instanceof final ComparableTable table) {
            this.convertRow(table.getRow(rowNum));
        } else {
            IDataSetConverter.super.convertRow(aTable, rowNum);
        }
    }

    @Override
    public void endTable() {
        // 行ごとに渡し終えている
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
    public IDataSetConverter splitForParallel(final ParallelTableMapping.WorkerResources resources) {
        return new RecordConverter(this.base, this.action, this.includeMetaData);
    }
}
