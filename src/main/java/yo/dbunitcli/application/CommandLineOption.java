package yo.dbunitcli.application;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.MapOptionHandler;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;
import yo.dbunitcli.application.setting.*;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.mapper.xlsx.XlsxSchema;
import yo.dbunitcli.writer.DataSetWriterParam;
import yo.dbunitcli.writer.IDataSetWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

abstract public class CommandLineOption {

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File("").getAbsoluteFile();

    @Option(name = "-resultType", usage = "csv | xls | xlsx | table ")
    private String resultType = "csv";

    @Option(name = "-outputEncoding", usage = "output csv file encoding")
    private String outputEncoding = "UTF-8";

    @Option(name = "-setting", usage = "file define comparison settings")
    private File setting;

    @Option(name = "-xlsxSchema", usage = "schema use read xlsx")
    private File xlsxSchemaSource;

    @Option(name = "-jdbcProperties", usage = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @Option(name = "-regDataSplit", usage = "regex to use split data row")
    private String regDataSplit;

    @Option(name = "-regHeaderSplit", usage = "regex to use split header row")
    private String regHeaderSplit;

    @Option(name = "-regInclude", usage = "regex to include table")
    private String regInclude;

    @Option(name = "-regExclude", usage = "regex to exclude table")
    private String regExclude;

    @Option(name = "-op", usage = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private String operation;

    @Option(name = "-P", handler = MapOptionHandler.class)
    Map<String, String> inputParam = Maps.newHashMap();

    @Option(name = "-excelTable", usage = "SHEET or BOOK")
    private String excelTable = "SHEET";

    @Option(name = "-exportEmptyTable", usage = "if true then empty table is not export")
    private String exportEmptyTable = "true";

    private final Parameter parameter;

    private Properties jdbcProp;

    private ColumnSettings columnSettings;

    private XlsxSchema xlsxSchema;

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

    public String getRegInclude() {
        return regInclude;
    }

    public String getRegExclude() {
        return regExclude;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public String getResultType() {
        return this.resultType;
    }

    public File getSetting() {
        return this.setting;
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

    public void parse(String[] args) throws Exception {
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
        return this.writer(this.resultDir);
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
                .setEncoding(this.getEncoding())
                .setColumnSettings(this.getColumnSettings())
                .setXlsxSchema(this.xlsxSchema)
                .setHeaderSplitPattern(this.getRegHeaderSplit())
                .setDataSplitPattern(this.getRegDataSplit())
                .setRegInclude(this.getRegInclude())
                .setRegExclude(this.getRegExclude());
    }

    protected DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return new DatabaseConnectionLoader(this.jdbcProp);
    }

    abstract protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException;

    protected void assertFileParameter(CmdLineParser parser, String source, File dir, String s) throws CmdLineException {
        final DataSourceType dataSourceType = DataSourceType.fromString(source);
        this.assertFileExists(parser, dir, s);
        if (dataSourceType == DataSourceType.TABLE || dataSourceType == DataSourceType.SQL) {
            if (this.jdbcProperties == null) {
                throw new CmdLineException(parser, dataSourceType + " need jdbcProperties option", new IllegalArgumentException());
            }
            if (!this.jdbcProperties.exists()) {
                throw new CmdLineException(parser, this.jdbcProperties.toString() + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
            }
        }
    }

    protected void assertFileExists(CmdLineParser parser, File dir, String s) throws CmdLineException {
        if (!dir.exists()) {
            throw new CmdLineException(parser, s + " is not exist", new IllegalArgumentException(dir.toString()));
        }
    }

    protected void loadJdbcTemplate() throws IOException {
        if (this.jdbcProperties != null) {
            this.jdbcProp = new Properties();
            this.jdbcProp.load(new FileInputStream(this.jdbcProperties));
        }
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
            this.xlsxSchema = new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource);
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }

    protected STGroup createSTGroup(File groupFile) {
        STGroup stGroup;
        if (groupFile == null) {
            stGroup = new STGroup('$', '$');
        } else {
            stGroup = new STGroupFile(groupFile.getAbsolutePath(), '$', '$');
        }
        stGroup.registerRenderer(String.class, new StringRenderer());
        return stGroup;
    }
}
