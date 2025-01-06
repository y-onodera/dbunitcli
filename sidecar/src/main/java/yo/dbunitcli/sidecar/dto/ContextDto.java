package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ContextDto {
    private String workspace;
    private String datasetBase;
    private String resultBase;
    private String settingBase;
    private String templateBase;
    private String jdbcBase;
    private String xlsxSchemaBase;

    public String getWorkspace() {
        return this.workspace;
    }

    public void setWorkspace(final String workspace) {
        this.workspace = workspace;
    }

    public String getDatasetBase() {
        return this.datasetBase;
    }

    public void setDatasetBase(final String datasetBase) {
        this.datasetBase = datasetBase;
    }

    public String getResultBase() {
        return this.resultBase;
    }

    public void setResultBase(final String resultBase) {
        this.resultBase = resultBase;
    }

    public String getSettingBase() {
        return this.settingBase;
    }

    public void setSettingBase(final String settingBase) {
        this.settingBase = settingBase;
    }

    public String getTemplateBase() {
        return this.templateBase;
    }

    public void setTemplateBase(final String templateBase) {
        this.templateBase = templateBase;
    }

    public String getJdbcBase() {
        return this.jdbcBase;
    }

    public void setJdbcBase(final String jdbcBase) {
        this.jdbcBase = jdbcBase;
    }

    public String getXlsxSchemaBase() {
        return this.xlsxSchemaBase;
    }

    public void setXlsxSchemaBase(final String xlsxSchemaBase) {
        this.xlsxSchemaBase = xlsxSchemaBase;
    }
}
