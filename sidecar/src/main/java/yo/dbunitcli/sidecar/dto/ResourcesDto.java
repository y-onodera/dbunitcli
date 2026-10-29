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

    private List<String> parameterizeTemplateFiles = new ArrayList<>();

    private List<String> fixedColumnDefs = new ArrayList<>();

    private List<String> sqlFiles = new ArrayList<>();

    private List<String> tableFiles = new ArrayList<>();

    private List<String> csvqFiles = new ArrayList<>();

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

    public List<String> getParameterizeTemplateFiles() {
        return this.parameterizeTemplateFiles;
    }

    public void setParameterizeTemplateFiles(final List<String> parameterizeTemplateFiles) {
        this.parameterizeTemplateFiles = parameterizeTemplateFiles;
    }

    public List<String> getFixedColumnDefs() {
        return this.fixedColumnDefs;
    }

    public void setFixedColumnDefs(final List<String> fixedColumnDefs) {
        this.fixedColumnDefs = fixedColumnDefs;
    }

    public List<String> getSqlFiles() {
        return this.sqlFiles;
    }

    public void setSqlFiles(final List<String> sqlFiles) {
        this.sqlFiles = sqlFiles;
    }

    public List<String> getTableFiles() {
        return this.tableFiles;
    }

    public void setTableFiles(final List<String> tableFiles) {
        this.tableFiles = tableFiles;
    }

    public List<String> getCsvqFiles() {
        return this.csvqFiles;
    }

    public void setCsvqFiles(final List<String> csvqFiles) {
        this.csvqFiles = csvqFiles;
    }

}
