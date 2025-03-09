package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class JsonXlsxSchemaRequestDto {
    private String name;

    private JsonXlsxSchemaDto input;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public JsonXlsxSchemaDto getInput() {
        return this.input;
    }

    public void setInput(final JsonXlsxSchemaDto input) {
        this.input = input;
    }
}
