package yo.dbunitcli.application.command;

import picocli.CommandLine;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;

public class ScaffoldDto extends CommandDto {

    private DataSetLoadDto datasetDto = new DataSetLoadDto();
    @CommandLine.Option(names = "-result", description = "directory to create workspace structure")
    private String resultDir;
    @CommandLine.Option(names = "-target", description = "target to generate: ddl, javaBean, or parameter (required, no default)")
    private String target;
    @CommandLine.Option(names = "-setting", description = "setting file name to scaffold (omit to skip)")
    private String settingName;
    @CommandLine.Option(names = "-template", description = "template file name to scaffold (omit to skip)")
    private String templateName;
    @CommandLine.Option(names = "-parameter", description = "parameter file name to scaffold (omit to skip)")
    private String parameterName;
    @CommandLine.Option(names = "-commandType", description = "commandType for parameter generation")
    private String commandType;

    private String[] commandInput = new String[0];

    public String getCommandType() {
        return this.commandType;
    }

    public void setCommandType(final String commandType) {
        this.commandType = commandType;
    }

    public String[] getCommandInput() {
        return this.commandInput;
    }

    public void setCommandInput(final String[] commandInput) {
        this.commandInput = commandInput;
    }

    public String getResultDir() {
        return this.resultDir;
    }

    public void setResultDir(final String resultDir) {
        this.resultDir = resultDir;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public String getSettingName() {
        return this.settingName;
    }

    public void setSettingName(final String settingName) {
        this.settingName = settingName;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public String getParameterName() {
        return this.parameterName;
    }

    public void setParameterName(final String parameterName) {
        this.parameterName = parameterName;
    }

    public DataSetLoadDto getDatasetDto() {
        return this.datasetDto;
    }

    public void setDatasetDto(final DataSetLoadDto datasetDto) {
        this.datasetDto = datasetDto;
    }

}
