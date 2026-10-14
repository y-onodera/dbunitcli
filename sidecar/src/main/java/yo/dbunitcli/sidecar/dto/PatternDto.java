package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatternDto {
    private String string;
    private List<String> exclude;
    private String filePath;

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    public String getString() {
        return this.string;
    }

    public void setString(final String string) {
        this.string = string;
    }

    public List<String> getExclude() {
        return this.exclude;
    }

    public void setExclude(final List<String> exclude) {
        this.exclude = exclude;
    }
}
