package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.dataset.DataSourceType;

@Serdeable
public class QueryDataSourceDto {

    private DataSourceType type;

    private String name;

    private String contents;

    public DataSourceType getType() {
        return this.type;
    }

    public void setType(final DataSourceType type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getContents() {
        return this.contents;
    }

    public void setContents(final String contents) {
        this.contents = contents;
    }
}
