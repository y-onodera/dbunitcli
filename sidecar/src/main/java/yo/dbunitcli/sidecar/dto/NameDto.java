package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class NameDto {
    private String any;
    private List<String> anyList;
    private String filePath;

    public String getAny() {
        return this.any;
    }

    public void setAny(final String any) {
        this.any = any;
    }

    public List<String> getAnyList() {
        return this.anyList;
    }

    public void setAnyList(final List<String> anyList) {
        this.anyList = anyList;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }
}