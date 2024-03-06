package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DBConverter;

import java.io.File;
import java.util.Map;

public class DataSetConverterOption extends DefaultArgumentsParser {

    private final JdbcOption jdbcOption;
    private final DataSetConsumerParam.Builder builder;
    @CommandLine.Option(names = "-result", description = "directory result files at")
    private File resultDir = new File(".");
    @CommandLine.Option(names = "-resultType")
    private ResultType resultType = ResultType.csv;
    @CommandLine.Option(names = "-exportEmptyTable", description = "if true then empty table is not export")
    private String exportEmptyTable = "true";
    @CommandLine.Option(names = "-resultPath", description = "result file relative path from -result=dir.")
    private String resultPath;
    @CommandLine.Option(names = "-outputEncoding", description = "output csv file encoding")
    private String outputEncoding = "UTF-8";
    @CommandLine.Option(names = "-op", description = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private DBConverter.Operation operation;
    @CommandLine.Option(names = "-excelTable", description = "SHEET or BOOK")
    private String excelTable = "SHEET";

    public DataSetConverterOption(final String prefix) {
        super(prefix);
        this.jdbcOption = new JdbcOption(prefix);
        this.builder = DataSetConsumerParam.builder();
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-resultType", this.resultType, ResultType.class);
        if (!result.hasValue("-resultType")) {
            return result;
        }
        final DataSourceType type = DataSourceType.valueOf(result.get("-resultType"));
        if (type == DataSourceType.table) {
            result.put("-op", this.operation == null ? DBConverter.Operation.CLEAN_INSERT : this.operation
                    , DBConverter.Operation.class);
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
        return result;
    }

    @Override
    public void setUpComponent(final String[] expandArgs) {
        if (this.resultType == ResultType.table) {
            this.jdbcOption.parseArgument(expandArgs);
        }
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

    public ResultType getResultType() {
        return this.resultType;
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

    public void setResultDir(final File resultDir) {
        this.resultDir = resultDir;
    }

    public void setResultPath(final String resultPath) {
        this.resultPath = resultPath;
    }

}
