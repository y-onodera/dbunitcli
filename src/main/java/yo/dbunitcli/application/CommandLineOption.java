package yo.dbunitcli.application;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

abstract public class CommandLineOption {

    private static Logger logger = LoggerFactory.getLogger(CommandLineOption.class);

    @Option(name = "-encoding", usage = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File("").getAbsoluteFile();

    @Option(name = "-resultType", usage = "csv | xls | xlsx | table : default csv")
    private String resultType = "csv";

    @Option(name = "-outputEncoding", usage = "output csv file encoding")
    private String outputEncoding = "UTF-8";

    @Option(name = "-setting", usage = "file define comparison settings")
    private File setting;

    @Option(name = "-jdbcProperties", usage = "use connect database. [url=,user=,pass=]")
    private File jdbcProperties;

    @Option(name = "-regDataSplit", usage = "regex to use split data row")
    private String regDataSplit;

    @Option(name = "-regHeaderSplit", usage = "regex to use split header row")
    private String regHeaderSplit;

    @Option(name = "-op", usage = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private String operation;

    private Properties jdbcProp;

    private final Parameter parameter;

    private ColumnSettings columnSettings;

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

    public File getResultDir() {
        return this.resultDir;
    }

    public String getResultType() {
        return this.resultType;
    }

    public File getSetting() {
        return this.setting;
    }

    public ColumnSetting getComparisonKeys() {
        return this.columnSettings.getComparisonKeys();
    }

    public ColumnSetting getExcludeColumns() {
        return this.columnSettings.getExcludeColumns();
    }

    public ColumnSetting getOrderColumns() {
        return this.columnSettings.getOrderColumns();
    }

    public Parameter getParameter() {
        return this.parameter;
    }

    public IDataSetWriter writer() throws DataSetException {
        return this.getDataSetWriter(this.getResultDir());
    }

    public ComparableDataSetLoaderParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetLoaderParam.builder()
                .setEncoding(this.getEncoding())
                .setExcludeColumns(this.getExcludeColumns())
                .setOrderColumns(this.getOrderColumns())
                .setHeaderSplitPattern(this.getRegHeaderSplit())
                .setDataSplitPattern(this.getRegDataSplit());
    }

    public ComparableDataSetLoader getComparableDataSetLoader() throws DataSetException {
        if (this.jdbcProp != null) {
            return new ComparableDataSetLoader(this.createIDatabaseConnection(), this.parameter);
        }
        return new ComparableDataSetLoader(this.parameter);
    }

    public void parse(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        parser.parseArgument(args);
        this.assertDirectoryExists(parser);
        this.loadJdbcTemplate();
        this.populateSettings(parser);
    }

    public IDatabaseConnection createIDatabaseConnection() throws DataSetException {
        String url = this.jdbcProp.get("url").toString();
        String user = this.jdbcProp.get("user").toString();
        String pass = this.jdbcProp.get("pass").toString();
        IDatabaseConnection result;
        try {
            if (url.contains("jdbc:oracle:thin")) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                result = new DatabaseConnection(conn, user);
                DatabaseConfig config = result.getConfig();
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
                config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, Boolean.TRUE);
                config.setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, Boolean.TRUE);
                config.setProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, Boolean.TRUE);
            } else if (url.contains("jdbc:sqlserver")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                result = new DatabaseConnection(conn, conn.getCatalog());
                DatabaseConfig config = result.getConfig();
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
                config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, Boolean.TRUE);
                config.setProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, Boolean.TRUE);
            } else {
                throw new UnsupportedOperationException("unknown url :" + url);
            }
            return result;
        } catch (ClassNotFoundException | SQLException | DatabaseUnitException ex) {
            throw new DataSetException(ex);
        }
    }

    abstract protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException;

    protected void assertFileParameter(CmdLineParser parser, String source, File dir, String s) throws CmdLineException {
        final DataSourceType dataSourceType = DataSourceType.fromString(source);
        if (dataSourceType.isNeedDir()) {
            if (!dir.exists() || !dir.isDirectory()) {
                throw new CmdLineException(parser, s + " is not exist directory", new IllegalArgumentException(dir.toString()));
            }
        } else {
            if (!dir.exists() || !dir.isFile()) {
                throw new CmdLineException(parser, s + " is not exist file", new IllegalArgumentException(dir.toString()));
            }
        }
        if (dataSourceType == DataSourceType.TABLE || dataSourceType == DataSourceType.SQL) {
            if (this.jdbcProperties == null) {
                throw new CmdLineException(parser, dataSourceType + " need jdbcProperties option", new IllegalArgumentException());
            }
            if (!this.jdbcProperties.exists()) {
                throw new CmdLineException(parser, this.jdbcProperties.toString() + " is not exist file", new IllegalArgumentException(this.jdbcProperties.toString()));
            }
        }
    }

    protected IDataSetWriter getDataSetWriter(File resultDir) throws DataSetException {
        logger.info("create DataSetWriter type:{} DBOperation:{} resultDir:{} encoding:{}"
                , this.resultType, this.operation, resultDir, this.outputEncoding);
        if (DataSourceType.XLSX.isEqual(this.resultType)) {
            return new XlsxDataSetWriter(resultDir);
        } else if (DataSourceType.XLS.isEqual(this.resultType)) {
            return new XlsDataSetWriter(resultDir);
        } else if (DataSourceType.TABLE.isEqual(this.resultType)) {
            return new DBDataSetWriter(this.createIDatabaseConnection(), this.operation);
        }
        return new CsvDataSetWriterWrapper(resultDir, this.outputEncoding);
    }

    protected void loadJdbcTemplate() throws IOException {
        if (this.jdbcProperties != null) {
            this.jdbcProp = new Properties();
            this.jdbcProp.load(new FileInputStream(this.jdbcProperties));
        }
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            if (this.setting != null) {
                this.columnSettings = ColumnSettings.builder().build(this.setting);
            } else {
                this.columnSettings = ColumnSettings.builder().build();
            }
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }

}
