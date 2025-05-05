package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.ArrayList;
import java.util.List;

@Serdeable
public class ResourcesDto {

    private List<String> datasetSettings = new ArrayList<>();

    private List<String> xlsxSchemas = new ArrayList<>();

    private List<String> jdbcFiles = new ArrayList<>();

    private List<String> templateFiles = new ArrayList<>();

    public List<String> getDatasetSettings() {
        return this.datasetSettings;
    }

    public List<String> getXlsxSchemas() {
        return xlsxSchemas;
    }

    public List<String> getJdbcFiles() {
        return jdbcFiles;
    }

    public List<String> getTemplateFiles() {
        return templateFiles;
    }

    public void setDatasetSettings(final List<String> datasetSettings) {
        this.datasetSettings = datasetSettings;
    }

    public void setXlsxSchemas(List<String> xlsxSchemas) {
        this.xlsxSchemas = xlsxSchemas;
    }

    public void setJdbcFiles(List<String> jdbcFiles) {
        this.jdbcFiles = jdbcFiles;
    }

    public void setTemplateFiles(List<String> templateFiles) {
        this.templateFiles = templateFiles;
    }
}
