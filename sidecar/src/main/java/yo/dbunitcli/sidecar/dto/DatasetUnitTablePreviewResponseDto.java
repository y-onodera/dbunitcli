package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.dataset.ComparableTable;

import java.util.List;

/**
 * unitSettingを適用した結果のテーブル1つ分のプレビュー
 */
@Serdeable
public class DatasetUnitTablePreviewResponseDto extends DatasetTablePreviewResponseDto {

    private String tableName;

    public DatasetUnitTablePreviewResponseDto(final String tableName, final String[] headers, final List<Object[]> rows, final int totalRows) {
        super(headers, rows, totalRows);
        this.tableName = tableName;
    }

    /**
     * tableのテーブル名・カラム名・全行数と、呼び出し側で件数を制限した先頭の一部の行rowsからなるプレビュー
     */
    public static DatasetUnitTablePreviewResponseDto of(final ComparableTable table, final List<Object[]> rows) {
        return new DatasetUnitTablePreviewResponseDto(table.getTableName(), DatasetTablePreviewResponseDto.headersOf(table), rows, table.getRowCount());
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }
}
