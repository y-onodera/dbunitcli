package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.writer.DBDataSetWriter;

import java.io.File;
import java.util.Map;

public class DataSetWriteOption extends PrefixArgumentsParser {

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
    private DBDataSetWriter.Operation operation;

    @Option(name = "-excelTable", usage = "SHEET or BOOK")
    private String excelTable = "SHEET";

    private final JdbcOption jdbcOption;

    private final DataSetWriterParam.Builder builder;

    public DataSetWriteOption(String prefix) {
        super(prefix);
        this.jdbcOption = new JdbcOption(prefix);
        this.builder = DataSetWriterParam.builder();
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        this.builder.setResultType(this.resultType);
        if (this.resultType == DataSourceType.table) {
            this.jdbcOption.parseArgument(expandArgs);
            this.builder.setOperation(this.operation)
                    .setDatabaseConnectionLoader(this.jdbcOption.getDatabaseConnectionLoader());
        } else {
            this.builder.setExportEmptyTable(Boolean.parseBoolean(this.exportEmptyTable));
            if (this.resultType == DataSourceType.csv) {
                this.builder.setOutputEncoding(this.outputEncoding);
            } else {
                this.builder.setExcelTable(this.excelTable);
            }
        }
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-resultType", ResultType.valueOf(this.resultType.toString()), ResultType.class);
        if (!result.hasValue("-resultType")) {
            return result;
        }
        try {
            DataSourceType type = DataSourceType.valueOf(result.get("-resultType"));
            if (type == DataSourceType.table) {
                result.put("-op", this.operation == null ? DBDataSetWriter.Operation.CLEAN_INSERT : this.operation
                        , DBDataSetWriter.Operation.class);
                result.putAll(this.jdbcOption.expandOption(args));
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

    public DataSetWriterParam.Builder getParam() {
        return builder;
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

    public enum ResultType {
        csv, xls, xlsx, table;
    }

}
