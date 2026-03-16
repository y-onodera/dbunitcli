package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ResolvePathRequestDto {

    private String path;

    private String defaultPath;

    private String srcType;

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getDefaultPath() {
        return this.defaultPath;
    }

    public void setDefaultPath(final String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public String getSrcType() {
        return this.srcType;
    }

    public void setSrcType(final String srcType) {
        this.srcType = srcType;
    }

}
