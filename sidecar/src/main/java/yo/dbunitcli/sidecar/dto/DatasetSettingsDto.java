package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class DatasetSettingsDto {

    private List<DatasetSettingDto> settings;

    private List<DatasetSettingDto> commonSettings;

    public List<DatasetSettingDto> getSettings() {
        return this.settings;
    }

    public void setSettings(final List<DatasetSettingDto> settings) {
        this.settings = settings;
    }

    public List<DatasetSettingDto> getCommonSettings() {
        return this.commonSettings;
    }

    public void setCommonSettings(final List<DatasetSettingDto> commonSettings) {
        this.commonSettings = commonSettings;
    }
}
