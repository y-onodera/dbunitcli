package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class PatternDto {
    private String string;
    private List<String> exclude;

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
