package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XlsxSheetsRequestDto {
    private String src;
    private String regTableInclude;
    private String regTableExclude;

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
}
