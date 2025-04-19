package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.converter.DBConverter;

public class GenerateDto extends CommandDto {
    @CommandLine.Option(names = "-generateType")
    private GenerateOption.GenerateType generateType;
    @CommandLine.Option(names = "-result", description = "directory result files at")
    private String resultDir;
    @CommandLine.Option(names = "-resultPath", description = "result file relative path from -result=dir.")
    private String resultPath;
    @CommandLine.Option(names = "-unit", description = "generate by row or table or dataset")
    private ParameterUnit unit;
    @CommandLine.Option(names = "-commit", description = "default commit;whether commit or not generate sql")
    private String commit;
    @CommandLine.Option(names = "-sqlFileSuffix", description = "generate sqlFile fileName suffix")
    private String sqlFileSuffix;
    @CommandLine.Option(names = "-sqlFilePrefix", description = "generate sqlFile fileName prefix")
    private String sqlFilePrefix;
    @CommandLine.Option(names = "-op")
    private DBConverter.Operation operation;
    @CommandLine.Option(names = "-outputEncoding", description = "output file encoding")
    private String outputEncoding;
    @CommandLine.Option(names = "-template", description = "template file")
    private String template;
    @CommandLine.Option(names = "-includeAllColumns", description = "include all columns in settings generation")
    private String includeAllColumns;
    private DataSetLoadDto srcData = new DataSetLoadDto();

    private TemplateRenderDto templateOption = new TemplateRenderDto();

    public GenerateOption.GenerateType getGenerateType() {
        return this.generateType;
    }

    public void setGenerateType(final GenerateOption.GenerateType generateType) {
        this.generateType = generateType;
    }

    public String getResultDir() {
        return this.resultDir;
    }

    public void setResultDir(final String resultDir) {
        this.resultDir = resultDir;
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public void setResultPath(final String resultPath) {
        this.resultPath = resultPath;
    }

    public ParameterUnit getUnit() {
        return this.unit;
    }

    public void setUnit(final ParameterUnit unit) {
        this.unit = unit;
    }

    public String getCommit() {
        return this.commit;
    }

    public void setCommit(final String commit) {
        this.commit = commit;
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

    public DBConverter.Operation getOperation() {
        return this.operation;
    }

    public void setOperation(final DBConverter.Operation operation) {
        this.operation = operation;
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

    public void setOutputEncoding(final String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getIncludeAllColumns() {
        return this.includeAllColumns;
    }

    public void setIncludeAllColumns(final String includeAllColumns) {
        this.includeAllColumns = includeAllColumns;
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
}
