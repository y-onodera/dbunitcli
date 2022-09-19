package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.consumer.DBConsumer;

import java.io.File;
import java.util.Map;

public class DataSetConsumerOption extends DefaultArgumentsParser {

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File(".");

    @Option(name = "-resultType")
    private DataSourceType resultType = DataSourceType.csv;

    @Option(name = "-exportEmptyTable", usage = "if true then empty table is not export")
    private String exportEmptyTable = "true";

    @Option(name = "-resultPath", usage = "result file relative path from -result=dir.")
    private String resultPath;

    @Option(name = "-outputEncoding", usage = "output csv file encoding")
    private String outputEncoding = "UTF-8";

    @Option(name = "-op", usage = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private DBConsumer.Operation operation;

    @Option(name = "-excelTable", usage = "SHEET or BOOK")
    private String excelTable = "SHEET";

    private final JdbcOption jdbcOption;

    private final DataSetConsumerParam.Builder builder;

    public DataSetConsumerOption(String prefix) {
        super(prefix);
        this.jdbcOption = new JdbcOption(prefix);
        this.builder = DataSetConsumerParam.builder();
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        if (this.resultType == DataSourceType.table) {
            this.jdbcOption.parseArgument(expandArgs);
        }
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-resultType", ResultType.valueOf(this.resultType.toString()), ResultType.class);
        if (!result.hasValue("-resultType")) {
            return result;
        }
        try {
            DataSourceType type = DataSourceType.valueOf(result.get("-resultType"));
            if (type == DataSourceType.table) {
                result.put("-op", this.operation == null ? DBConsumer.Operation.CLEAN_INSERT : this.operation
                        , DBConsumer.Operation.class);
                result.putAll(this.jdbcOption.createOptionParam(args));
            } else {
                result.putDir("-result", this.resultDir);
                result.put("-resultPath", this.resultPath);
                result.put("-exportEmptyTable", this.exportEmptyTable);
                if (type == DataSourceType.csv) {
                    result.put("-outputEncoding", this.outputEncoding);
                } else {
                    result.put("-excelTable", this.excelTable);
                }
            }
        } catch (Throwable th) {
        }
        return result;
    }

    public DataSetConsumerParam.Builder getParam() {
        return this.builder
                .setResultType(this.resultType)
                .setExportEmptyTable(Boolean.parseBoolean(this.exportEmptyTable))
                .setResultDir(this.resultDir)
                .setResultPath(this.resultPath)
                .setOperation(this.operation)
                .setDatabaseConnectionLoader(this.jdbcOption.getDatabaseConnectionLoader())
                .setOutputEncoding(this.outputEncoding)
                .setExcelTable(this.excelTable)
                ;
    }

    public DataSourceType getResultType() {
        return resultType;
    }

    public JdbcOption getJdbcOption() {
        return this.jdbcOption;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

    public void setResultDir(File resultDir) {
        this.resultDir = resultDir;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public enum ResultType {
        csv, xls, xlsx, table;
    }

}
