package yo.dbunitcli.application.command;

import picocli.CommandLine;
import yo.dbunitcli.application.CommandDto;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.command.generate.XlsxTemplateType;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.application.dto.TemplateRenderDto;
import yo.dbunitcli.dataset.DbOperation;

public class GenerateDto extends CommandDto {
    @CommandLine.Option(names = "-generateType")
    private GenerateType generateType;
    @CommandLine.Option(names = "-result", description = "directory result files at")
    private String resultDir;
    @CommandLine.Option(names = "-resultPath", description = "result file relative path from -result=dir.")
    private String resultPath;
    @CommandLine.Option(names = "-unit", description = "generate by row or table or dataset")
    private ParameterUnit unit;
    @CommandLine.Option(names = "-unitSetting", description = "settings file used to resolve rows for table unit template generation")
    private String unitSetting;
    @CommandLine.Option(names = "-unitSettingEncoding", description = "encoding of unitSetting file")
    private String unitSettingEncoding;
    @CommandLine.Option(names = "-commit", description = "default commit;whether commit or not generate sql")
    private String commit;
    @CommandLine.Option(names = "-sqlFileSuffix", description = "generate sqlFile fileName suffix")
    private String sqlFileSuffix;
    @CommandLine.Option(names = "-sqlFilePrefix", description = "generate sqlFile fileName prefix")
    private String sqlFilePrefix;
    @CommandLine.Option(names = "-op")
    private DbOperation operation;
    @CommandLine.Option(names = "-outputEncoding", description = "output file encoding")
    private String outputEncoding;
    @CommandLine.Option(names = "-template", description = "template file")
    private String template;
    @CommandLine.Option(names = "-includeAllColumns", description = "include all columns in settings generation")
    private String includeAllColumns;
    @CommandLine.Option(names = "-lazyLoad", description = "use lazy load for excel generation")
    private String lazyLoad;
    @CommandLine.Option(names = "-fixedLength", description = "comma-separated column lengths for fixedColumnDef generation")
    private String fixedLength;
    @CommandLine.Option(names = "-defaultLength", description = "default column length for fixedColumnDef generation")
    private String defaultLength;
    @CommandLine.Option(names = "-align", description = "column alignment for fixedColumnDef generation (left or right)")
    private String align;
    @CommandLine.Option(names = "-templateType", description = "xlsxTemplate generation: table (sheet per table), compare (oldTable and newTable sheets and a compare sheet) or metadata (table definition document filled with -src.loadMetaData rows by -generateType=xlsx -unit=table)")
    private XlsxTemplateType templateType;
    @CommandLine.Option(names = "-oldTable", description = "table name in src (after setting) used as the old data sheet for compare xlsxTemplate")
    private String oldTable;
    @CommandLine.Option(names = "-newTable", description = "table name in src (after setting) used as the new data sheet for compare xlsxTemplate")
    private String newTable;
    @CommandLine.Option(names = "-keys", description = "comma-separated key columns for compare xlsxTemplate. oldColumn:newColumn or column")
    private String keys;
    @CommandLine.Option(names = "-compares", description = "comma-separated columns to compare for compare xlsxTemplate. oldColumn:newColumn or column")
    private String compares;
    @CommandLine.Option(names = "-compareSheetName", description = "compare sheet name for compare xlsxTemplate")
    private String compareSheetName;
    @CommandLine.Option(names = "-parallel", description = "number of threads to load sources and generate per table in parallel. 1 is sequential (default: CPU cores if possible)")
    private String parallel;
    private DataSetLoadDto srcData = new DataSetLoadDto();

    private TemplateRenderDto templateOption = new TemplateRenderDto();

    public GenerateType getGenerateType() {
        return this.generateType;
    }

    public void setGenerateType(final GenerateType generateType) {
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

    public String getUnitSetting() {
        return this.unitSetting;
    }

    public void setUnitSetting(final String unitSetting) {
        this.unitSetting = unitSetting;
    }

    public String getUnitSettingEncoding() {
        return this.unitSettingEncoding;
    }

    public void setUnitSettingEncoding(final String unitSettingEncoding) {
        this.unitSettingEncoding = unitSettingEncoding;
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

    public DbOperation getOperation() {
        return this.operation;
    }

    public void setOperation(final DbOperation operation) {
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

    public String getLazyLoad() {
        return this.lazyLoad;
    }

    public void setLazyLoad(final String lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

    public String getFixedLength() {
        return this.fixedLength;
    }

    public void setFixedLength(final String fixedLength) {
        this.fixedLength = fixedLength;
    }

    public String getDefaultLength() {
        return this.defaultLength;
    }

    public void setDefaultLength(final String defaultLength) {
        this.defaultLength = defaultLength;
    }

    public String getAlign() {
        return this.align;
    }

    public void setAlign(final String align) {
        this.align = align;
    }

    public XlsxTemplateType getTemplateType() {
        return this.templateType;
    }

    public void setTemplateType(final XlsxTemplateType templateType) {
        this.templateType = templateType;
    }

    public String getOldTable() {
        return this.oldTable;
    }

    public void setOldTable(final String oldTable) {
        this.oldTable = oldTable;
    }

    public String getNewTable() {
        return this.newTable;
    }

    public void setNewTable(final String newTable) {
        this.newTable = newTable;
    }

    public String getKeys() {
        return this.keys;
    }

    public void setKeys(final String keys) {
        this.keys = keys;
    }

    public String getCompares() {
        return this.compares;
    }

    public void setCompares(final String compares) {
        this.compares = compares;
    }

    public String getCompareSheetName() {
        return this.compareSheetName;
    }

    public void setCompareSheetName(final String compareSheetName) {
        this.compareSheetName = compareSheetName;
    }

    public String getParallel() {
        return this.parallel;
    }

    public void setParallel(final String parallel) {
        this.parallel = parallel;
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
