package yo.dbunitcli.sidecar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class DatasetSettingsDto {

    private List<DatasetSettingDto> settings;

    private List<DatasetSettingDto> commonSettings;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OutputFilterDto outputFilter;

    @JsonProperty("import")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ImportDto> imports;

    public List<ImportDto> getImports() {
        return this.imports;
    }

    public void setImports(final List<ImportDto> imports) {
        this.imports = imports;
    }

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

    public OutputFilterDto getOutputFilter() {
        return this.outputFilter;
    }

    public void setOutputFilter(final OutputFilterDto outputFilter) {
        this.outputFilter = outputFilter;
    }
}
