package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XlsxSheetsRequestDto {
    private String src;
    private String regTableInclude;
    private String regTableExclude;
    private boolean recursive;
    private String regInclude;
    private String regExclude;
    private String extension;

    public String getSrc() {
        return this.src;
    }

    public void setSrc(final String src) {
        this.src = src;
    }

    public String getRegTableInclude() {
        return this.regTableInclude;
    }

    public void setRegTableInclude(final String regTableInclude) {
        this.regTableInclude = regTableInclude;
    }

    public String getRegTableExclude() {
        return this.regTableExclude;
    }

    public void setRegTableExclude(final String regTableExclude) {
        this.regTableExclude = regTableExclude;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    public void setRecursive(final boolean recursive) {
        this.recursive = recursive;
    }

    public String getRegInclude() {
        return this.regInclude;
    }

    public void setRegInclude(final String regInclude) {
        this.regInclude = regInclude;
    }

    public String getRegExclude() {
        return this.regExclude;
    }

    public void setRegExclude(final String regExclude) {
        this.regExclude = regExclude;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }
}
