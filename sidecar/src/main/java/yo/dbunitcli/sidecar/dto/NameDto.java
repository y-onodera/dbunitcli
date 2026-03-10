package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NameDto {
    private List<String> any;
    private String filePath;

    public List<String> getAny() {
        return this.any;
    }

    public void setAny(final List<String> any) {
        this.any = any;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }
}
