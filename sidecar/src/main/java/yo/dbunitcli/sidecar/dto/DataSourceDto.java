package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.dataset.DataSourceType;

@Serdeable
public class DataSourceDto {

    private DataSourceType type;

    private String fileName;

    private String contents;

    public DataSourceType getType() {
        return this.type;
    }

    public void setType(final DataSourceType type) {
        this.type = type;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getContents() {
        return this.contents;
    }

    public void setContents(final String contents) {
        this.contents = contents;
    }
}
