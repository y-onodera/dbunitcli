package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.cli.CommandDto;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.converter.DBConverter;

public class GenerateDto extends CommandDto {
    @CommandLine.Option(names = "-generateType")
    private GenerateOption.GenerateType generateType = GenerateOption.GenerateType.txt;
    @CommandLine.Option(names = "-unit")
    private GenerateOption.GenerateUnit unit = GenerateOption.GenerateUnit.record;
    @CommandLine.Option(names = "-commit", description = "default commit;whether commit or not generate sql")
    private String commit = "true";
    @CommandLine.Option(names = "-sqlFileSuffix", description = "generate sqlFile fileName suffix")
    private String sqlFileSuffix = "";
    @CommandLine.Option(names = "-sqlFilePrefix", description = "generate sqlFile fileName prefix")
    private String sqlFilePrefix = "";
    @CommandLine.Option(names = "-op")
    private DBConverter.Operation operation;
    @CommandLine.Option(names = "-outputEncoding", description = "output file encoding")
    private String outputEncoding = "UTF-8";
    @CommandLine.Option(names = "-template", description = "template file")
    private String template;
    private DataSetLoadDto dataSetLoad = new DataSetLoadDto();

    private TemplateRenderDto templateRender = new TemplateRenderDto();

    public GenerateOption.GenerateType getGenerateType() {
        return this.generateType;
    }

    public void setGenerateType(final GenerateOption.GenerateType generateType) {
        this.generateType = generateType;
    }

    public GenerateOption.GenerateUnit getUnit() {
        return this.unit;
    }

    public void setUnit(final GenerateOption.GenerateUnit unit) {
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
}
