package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class MetaDataSettingsDto {

    private List<MetaDataSettingDto> settings;

    private List<MetaDataSettingDto> commonSettings;

    public List<MetaDataSettingDto> getSettings() {
        return this.settings;
    }

    public void setSettings(final List<MetaDataSettingDto> settings) {
        this.settings = settings;
    }

    public List<MetaDataSettingDto> getCommonSettings() {
        return this.commonSettings;
    }

    public void setCommonSettings(final List<MetaDataSettingDto> commonSettings) {
        this.commonSettings = commonSettings;
    }
}
