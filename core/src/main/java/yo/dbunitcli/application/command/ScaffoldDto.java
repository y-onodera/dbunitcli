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
    @CommandLine.Option(names = "-generateTargets", split = ",", description = "targets to generate: ddl,javaBean")
    private List<String> generateTargets;

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

    public List<String> getGenerateTargets() {
        return this.generateTargets;
    }

    public void setGenerateTargets(final List<String> generateTargets) {
        this.generateTargets = generateTargets;
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
