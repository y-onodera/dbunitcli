package yo.dbunitcli.application;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.dataset.writer.DataSetWriterLoader;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.resource.st4.TemplateRender;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

abstract public class CommandLineOption {

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File(".");

    @Option(name = "-resultPath", usage = "result file relative path from -result=dir.")
    private String resultPath;

    @Option(name = "-resultType", usage = "csv | xls | xlsx | table ")
    private String resultType = "csv";

    @Option(name = "-outputEncoding", usage = "output csv file encoding")
    private String outputEncoding = "UTF-8";

    @Option(name = "-setting", usage = "file define comparison settings")
    private File setting;

    @Option(name = "-loadData", usage = "default true. if false data row didn't load")
    private String loadData = "true";

    @Option(name = "-headerName", usage = "comma separate header name. if set,all rows treat data rows")
    private String headerName;

    @Option(name = "-xlsxSchema", usage = "schema use read xlsx")
    private File xlsxSchemaSource;

    @Option(name = "-jdbcProperties", usage = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @Option(name = "-jdbcUrl", usage = "use connect database. override jdbcProperties value")
    private String jdbcUrl;

    @Option(name = "-jdbcUser", usage = "use connect database. override jdbcProperties value")
    private String jdbcUser;

    @Option(name = "-jdbcPass", usage = "use connect database. override jdbcProperties value")
    private String jdbcPass;

    @Option(name = "-useJdbcMetaData", usage = "default false. whether load metaData from jdbc or not")
    private String useJdbcMetaData = "false";

    @Option(name = "-regDataSplit", usage = "regex to use split data row")
    private String regDataSplit;

    @Option(name = "-regHeaderSplit", usage = "regex to use split header row")
    private String regHeaderSplit;

    @Option(name = "-fixedLength", usage = "comma separate column Lengths")
    private String fixedLength;

    @Option(name = "-regInclude", usage = "regex to include table")
    private String regInclude;

    @Option(name = "-regExclude", usage = "regex to exclude table")
    private String regExclude;

    @Option(name = "-op", usage = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private String operation;

    @Option(name = "-excelTable", usage = "SHEET or BOOK")
    private String excelTable = "SHEET";

    @Option(name = "-exportEmptyTable", usage = "if true then empty table is not export")
    private String exportEmptyTable = "true";

    @Option(name = "-template", usage = "template file. generate file convert outputEncoding")
    private File template;

    @Option(name = "-templateEncoding", usage = "template file encoding.default is encoding option")
    private String templateEncoding;

    @Option(name = "-templateGroup", usage = "StringTemplate4 templateGroup file.")
    private File templateGroup;

    @Option(name = "-templateParameterAttribute", usage = "attributeName that is used to for access parameter in StringTemplate expression default 'param'.")
    private String templateParameterAttribute = "param";

    @Option(name = "-templateVarStart", usage = "StringTemplate expression start char.default '$'")
    private char templateVarStart = '$';

    @Option(name = "-templateVarStop", usage = "StringTemplate expression stop char.default '$'\"")
    private char templateVarStop = '$';

    @Option(name = "-P", handler = MapOptionHandler.class)
    private Map<String, String> inputParam = Maps.newHashMap();

    private final Parameter parameter;

    private Properties jdbcProp;

    private ColumnSettings columnSettings;

    private XlsxSchema xlsxSchema;

    private String[] args;

    public CommandLineOption(Parameter param) {
        this.parameter = param;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

    public String getRegDataSplit() {
        return this.regDataSplit;
    }

    public String getRegHeaderSplit() {
        return this.regHeaderSplit;
    }

    public String getFixedLength() {
        return fixedLength;
    }

    public String getRegInclude() {
        return regInclude;
    }

    public String getRegExclude() {
        return regExclude;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public File getResultFile() {
        return new File(this.resultDir, this.resultPath);
    }

    public String getResultType() {
        return this.resultType;
    }

    public File getSetting() {
        return this.setting;
    }

    public String getOperation() {
        return operation;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.columnSettings.getComparisonKeys();
    }

    public ColumnSettings getColumnSettings() {
        return this.columnSettings;
    }

    public Parameter getParameter() {
        return this.parameter;
    }

    public File getTemplate() {
        return this.template;
    }

    public String getTemplateEncoding() {
        return Strings.isNullOrEmpty(this.templateEncoding) ? this.encoding : this.templateEncoding;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public void setLoadData(String loadData) {
        this.loadData = loadData;
    }

    public void setUseJdbcMetaData(String useJdbcMetaData) {
        this.useJdbcMetaData = useJdbcMetaData;
    }

    public void parse(String[] args) throws Exception {
        this.args = args;
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            this.assertDirectoryExists(parser);
            this.loadJdbcTemplate();
            this.populateSettings(parser);
            this.parameter.getMap().putAll(this.inputParam);
        } catch (CmdLineException cx) {
            System.out.println("usage:");
            parser.printSingleLineUsage(System.out);
            System.out.println();
            parser.printUsage(System.out);
            throw cx;
        }
    }

    public IDataSetWriter writer() throws DataSetException {
        return this.writer(this.getResultDir());
    }

    public IDataSetWriter writer(File outputTo) throws DataSetException {
        return new DataSetWriterLoader().get(
                DataSetWriterParam.builder()
                        .setResultType(this.resultType)
                        .setOperation(this.operation)
                        .setDatabaseConnectionLoader(this.getDatabaseConnectionLoader())
                        .setResultDir(outputTo)
                        .setOutputEncoding(this.outputEncoding)
                        .setExcelTable(this.excelTable)
                        .setExportEmptyTable(Boolean.parseBoolean(this.exportEmptyTable))
                        .build());
    }

    protected ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.getDatabaseConnectionLoader(), this.parameter);
    }

    protected ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder()
                .setUseJdbcMetaData(Boolean.parseBoolean(this.useJdbcMetaData))
                .setEncoding(this.getEncoding())
                .setColumnSettings(this.getColumnSettings())
                .setLoadData(Boolean.parseBoolean(this.loadData))
                .setHeaderName(this.headerName)
                .setXlsxSchema(this.xlsxSchema)
                .setHeaderSplitPattern(this.getRegHeaderSplit())
                .setDataSplitPattern(this.getRegDataSplit())
                .setFixedLength(this.getFixedLength())
                .setRegInclude(this.getRegInclude())
                .setRegExclude(this.getRegExclude())
                .setSTTemplateLoader(this.getTemplateRender())
                ;
    }

    protected DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return new DatabaseConnectionLoader(this.jdbcProp);
    }

    protected TemplateRender getTemplateRender() {
        return TemplateRender.builder()
                .setTemplateGroup(this.templateGroup)
                .setTemplateVarStart(this.templateVarStart)
                .setTemplateVarStop(this.templateVarStop)
                .setTemplateParameterAttribute(this.templateParameterAttribute)
                .setEncoding(this.getTemplateEncoding())
                .build();
    }

    abstract protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException;

    protected void assertFileParameter(CmdLineParser parser, String source, File dir, String s) throws CmdLineException {
        final DataSourceType dataSourceType = DataSourceType.fromString(source);
        this.assertFileExists(parser, dir, s);
        if (dataSourceType.fromDatabase()) {
            if (Stream.of(this.jdbcUrl, this.jdbcUser, this.jdbcPass)
                    .anyMatch(Strings::isNullOrEmpty)) {
                if (this.jdbcProperties == null) {
                    throw new CmdLineException(parser, dataSourceType + " need jdbcProperties option", new IllegalArgumentException());
                }
                if (!this.jdbcProperties.exists()) {
                    throw new CmdLineException(parser, this.jdbcProperties.toString() + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
                }
            }
        }
    }

    protected void assertFileExists(CmdLineParser parser, File file, String s) throws CmdLineException {
        if (!file.exists()) {
            throw new CmdLineException(parser, s + " is not exist", new IllegalArgumentException(file.toString()));
        }
    }

    protected void loadJdbcTemplate() throws IOException {
        this.jdbcProp = new Properties();
        if (this.jdbcProperties != null) {
            this.jdbcProp.load(new FileInputStream(this.jdbcProperties));
        }
        if (!Strings.isNullOrEmpty(this.jdbcUrl)) {
            this.jdbcProp.put("url", this.jdbcUrl);
        }
        if (!Strings.isNullOrEmpty(this.jdbcUser)) {
            this.jdbcProp.put("user", this.jdbcUser);
        }
        if (!Strings.isNullOrEmpty(this.jdbcPass)) {
            this.jdbcProp.put("pass", this.jdbcPass);
        }
    }

    protected String loadTemplateString() throws IOException {
        return this.getTemplateRender().toString(this.getTemplate());
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            if (Strings.isNullOrEmpty(this.resultPath)) {
                this.setResultPath(this.resultName());
            }
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
            this.xlsxSchema = new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource);
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }

    protected String resultName() {
        String resultFile = "result";
        if (args[0].startsWith("@")) {
            resultFile = new File(args[0].replace("@", "")).getName();
            resultFile = resultFile.substring(0, resultFile.lastIndexOf("."));
        }
        return resultFile;
    }

}
