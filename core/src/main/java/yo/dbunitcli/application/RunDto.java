package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.JdbcDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;

public class RunDto extends CommandDto {

    @CommandLine.Option(names = "-scriptType")
    private RunOption.ScriptType scriptType;

    private DataSetLoadDto srcData = new DataSetLoadDto();

    private TemplateRenderDto templateOption = new TemplateRenderDto();

    private JdbcDto jdbcOption = new JdbcDto();

    public RunOption.ScriptType getScriptType() {
        return this.scriptType;
    }

    public void setScriptType(final RunOption.ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public DataSetLoadDto getSrcData() {
        return this.srcData;
    }

    public void setSrcData(final DataSetLoadDto srcData) {
        this.srcData = srcData;
    }

    public TemplateRenderDto getTemplateOption() {
        return this.templateOption;
    }

    public void setTemplateOption(final TemplateRenderDto templateOption) {
        this.templateOption = templateOption;
    }

    public JdbcDto getJdbcOption() {
        return this.jdbcOption;
    }

    public void setJdbcOption(final JdbcDto jdbcOption) {
        this.jdbcOption = jdbcOption;
    }
}
