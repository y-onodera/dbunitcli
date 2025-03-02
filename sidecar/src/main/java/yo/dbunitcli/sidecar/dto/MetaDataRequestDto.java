package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class MetaDataRequestDto {
    private String name;

    private MetaDataSettingsDto input;

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public MetaDataSettingsDto getInput() {
        return this.input;
    }

    public void setInput(final MetaDataSettingsDto input) {
        this.input = input;
    }
}
