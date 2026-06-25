package yo.dbunitcli.application.command;

import picocli.CommandLine;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;

import java.util.List;

public class ScaffoldDto extends CommandDto {
    @CommandLine.Option(names = "-result", description = "directory to create workspace structure")
    private String resultDir;
    @CommandLine.Option(names = "-sqlFileSuffix", description = "generate sqlFile fileName suffix")
    private String sqlFileSuffix;
    @CommandLine.Option(names = "-sqlFilePrefix", description = "generate sqlFile fileName prefix")
    private String sqlFilePrefix;
    @CommandLine.Option(names = "-target", description = "target to generate: ddl, javaBean, or parameter (required, no default)")
    private String target;
    @CommandLine.Option(names = "-ddlIncludes", split = ",", description = "scaffold contents for ddl: setting,template,parameter (default: all)")
    private List<String> ddlIncludes;
    @CommandLine.Option(names = "-javaBeanIncludes", split = ",", description = "scaffold contents for javaBean: setting,template,parameter (default: all)")
    private List<String> javaBeanIncludes;
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

    private DataSetLoadDto srcData = new DataSetLoadDto();
    private DataSetConverterDto datasetResult = new DataSetConverterDto();

    public String getResultDir() {
        return this.resultDir;
    }

    public void setResultDir(final String resultDir) {
        this.resultDir = resultDir;
    }

    public String getSqlFileSuffix() {
        return this.sqlFileSuffix;
    }

    public void setSqlFileSuffix(final String sqlFileSuffix) {
        this.sqlFileSuffix = sqlFileSuffix;
    }

    public String getSqlFilePrefix() {
        return this.sqlFilePrefix;
    }

    public void setSqlFilePrefix(final String sqlFilePrefix) {
        this.sqlFilePrefix = sqlFilePrefix;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public List<String> getDdlIncludes() {
        return this.ddlIncludes;
    }

    public void setDdlIncludes(final List<String> ddlIncludes) {
        this.ddlIncludes = ddlIncludes;
    }

    public List<String> getJavaBeanIncludes() {
        return this.javaBeanIncludes;
    }

    public void setJavaBeanIncludes(final List<String> javaBeanIncludes) {
        this.javaBeanIncludes = javaBeanIncludes;
    }

    public DataSetLoadDto getSrcData() {
        return this.srcData;
    }

    public void setSrcData(final DataSetLoadDto srcData) {
        this.srcData = srcData;
    }

    public DataSetConverterDto getDatasetResult() {
        return this.datasetResult;
    }

    public void setDatasetResult(final DataSetConverterDto datasetResult) {
        this.datasetResult = datasetResult;
    }
}
