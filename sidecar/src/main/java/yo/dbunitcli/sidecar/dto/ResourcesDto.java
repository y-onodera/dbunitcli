package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;

@Serdeable
public class ResourcesDto {

    private List<String> datasetSettings = new ArrayList<>();

    public List<String> getDatasetSettings() {
        return this.datasetSettings;
    }

    public void setDatasetSettings(final List<String> datasetSettings) {
        this.datasetSettings = datasetSettings;
    }
}
