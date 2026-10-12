package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class TemplateContentDto {

    private String content;

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
