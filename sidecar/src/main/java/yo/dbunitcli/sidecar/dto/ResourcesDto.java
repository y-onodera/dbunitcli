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

    private QueryFilesDto queryFiles = new QueryFilesDto();

    public List<String> getDatasetSettings() {
        return this.datasetSettings;
    }

    public List<String> getXlsxSchemas() {
        return this.xlsxSchemas;
    }

    public List<String> getJdbcFiles() {
        return this.jdbcFiles;
    }

    public List<String> getTemplateFiles() {
        return this.templateFiles;
    }

    public QueryFilesDto getQueryFiles() {
        return this.queryFiles;
    }

    public void setDatasetSettings(final List<String> datasetSettings) {
        this.datasetSettings = datasetSettings;
    }

    public void setXlsxSchemas(final List<String> xlsxSchemas) {
        this.xlsxSchemas = xlsxSchemas;
    }

    public void setJdbcFiles(final List<String> jdbcFiles) {
        this.jdbcFiles = jdbcFiles;
    }

    public void setTemplateFiles(final List<String> templateFiles) {
        this.templateFiles = templateFiles;
    }

    public void setQueryFiles(final QueryFilesDto queryFiles) {
        this.queryFiles = queryFiles;
    }
}
