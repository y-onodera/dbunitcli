package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class FixedColumnDefDto {

    private List<ColumnDefSetting> columns;

    public List<ColumnDefSetting> getColumns() {
        return this.columns;
    }

    public void setColumns(final List<ColumnDefSetting> columns) {
        this.columns = columns;
    }

    @Serdeable
    public record ColumnDefSetting(String name, int length, String align, String pad) {
    }
}
