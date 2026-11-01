package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

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

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }
}
