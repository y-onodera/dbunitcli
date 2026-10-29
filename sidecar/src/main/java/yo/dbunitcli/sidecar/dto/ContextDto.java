package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ContextDto {
    private String workspace;
    private String datasetBase;
    private String resultBase;
    private String env;
    private String settingBase;
    private String templateBase;
    private String parameterizeTemplateBase;
    private String jdbcBase;
    private String xlsxSchemaBase;
    private String fixedColumnDefBase;
    private String sqlBase;
    private String tableBase;
    private String csvqBase;

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

    public String getEnv() {
        return this.env;
    }

    public void setEnv(final String env) {
        this.env = env;
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

    public String getParameterizeTemplateBase() {
        return this.parameterizeTemplateBase;
    }

    public void setParameterizeTemplateBase(final String parameterizeTemplateBase) {
        this.parameterizeTemplateBase = parameterizeTemplateBase;
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

    public String getFixedColumnDefBase() {
        return this.fixedColumnDefBase;
    }

    public void setFixedColumnDefBase(final String fixedColumnDefBase) {
        this.fixedColumnDefBase = fixedColumnDefBase;
    }

    public String getSqlBase() {
        return this.sqlBase;
    }

    public void setSqlBase(final String sqlBase) {
        this.sqlBase = sqlBase;
    }

    public String getTableBase() {
        return this.tableBase;
    }

    public void setTableBase(final String tableBase) {
        this.tableBase = tableBase;
    }

    public String getCsvqBase() {
        return this.csvqBase;
    }

    public void setCsvqBase(final String csvqBase) {
        this.csvqBase = csvqBase;
    }

}
