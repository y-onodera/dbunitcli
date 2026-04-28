package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class JdbcColumnsRequestDto extends JdbcDto {

    private String table;

    public String getTable() {
        return this.table;
    }

    public void setTable(final String table) {
        this.table = table;
    }
}
