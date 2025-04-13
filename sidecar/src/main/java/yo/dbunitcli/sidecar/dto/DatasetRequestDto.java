package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class DatasetRequestDto implements ResourceSaveRequest<DatasetSettingsDto> {
    private String name;

    private DatasetSettingsDto input;

    public String getName() {
        return this.name;
    }

    public DatasetSettingsDto getInput() {
        return this.input;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setInput(final DatasetSettingsDto input) {
        this.input = input;
    }
}
