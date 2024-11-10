package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class MetaDataRequestDto {
    private String name;

    private MetaDataSettingsDto input;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetaDataSettingsDto getInput() {
        return input;
    }

    public void setInput(MetaDataSettingsDto input) {
        this.input = input;
    }
}
