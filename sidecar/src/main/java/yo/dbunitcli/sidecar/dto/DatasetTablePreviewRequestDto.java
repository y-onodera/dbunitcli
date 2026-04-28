package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class DatasetTablePreviewRequestDto extends DatasetTableNamesRequestDto {

    private String tableName;

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }
}
