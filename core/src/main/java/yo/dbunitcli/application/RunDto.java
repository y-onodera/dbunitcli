package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.JdbcDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;

public class RunDto extends CommandDto {

    @CommandLine.Option(names = "-scriptType")
    private RunOption.ScriptType scriptType = RunOption.ScriptType.sql;

    private DataSetLoadDto dataSetLoad = new DataSetLoadDto();

    private TemplateRenderDto templateRender = new TemplateRenderDto();

    private JdbcDto jdbc = new JdbcDto();

    public RunOption.ScriptType getScriptType() {
        return this.scriptType;
    }

    public void setScriptType(final RunOption.ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public DataSetLoadDto getDataSetLoad() {
        return this.dataSetLoad;
    }

    public void setDataSetLoad(final DataSetLoadDto dataSetLoad) {
        this.dataSetLoad = dataSetLoad;
    }

    public TemplateRenderDto getTemplateRender() {
        return this.templateRender;
    }

    public void setTemplateRender(final TemplateRenderDto templateRender) {
        this.templateRender = templateRender;
    }

    public JdbcDto getJdbc() {
        return this.jdbc;
    }

    public void setJdbc(final JdbcDto jdbc) {
        this.jdbc = jdbc;
    }
}
