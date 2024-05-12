package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DBConverter;

import java.io.File;

public class DataSetConverterOption implements Option {

    private final String prefix;
    private final JdbcOption jdbcOption;
    private final DBConverter.Operation operation;
    private final ResultType resultType;
    private final boolean exportEmptyTable;
    private final String outputEncoding;
    private final File resultDir;
    private final String resultPath;
    private final String excelTable;

    public DataSetConverterOption(final String prefix, final DataSetConverterDto dto) {
        this.prefix = prefix;
        if (dto.getResultType() != null) {
            this.resultType = dto.getResultType();
        } else {
            this.resultType = ResultType.csv;
        }
        if (Strings.isNotEmpty(dto.getResultDir())) {
            this.resultDir = new File(dto.getResultDir());
        } else {
            this.resultDir = new File(".");
        }
        this.resultPath = dto.getResultPath();
        if (Strings.isNotEmpty(dto.getExportEmptyTable())) {
            this.exportEmptyTable = Boolean.parseBoolean(dto.getExportEmptyTable());
        } else {
            this.exportEmptyTable = true;
        }
        if (Strings.isNotEmpty(dto.getOutputEncoding())) {
            this.outputEncoding = dto.getOutputEncoding();
        } else {
            this.outputEncoding = "UTF-8";
        }
        this.operation = dto.getOperation();
        if (Strings.isNotEmpty(dto.getExcelTable())) {
            this.excelTable = dto.getExcelTable();
        } else {
            this.excelTable = "SHEET";
        }
        if (this.resultType == ResultType.table) {
            this.jdbcOption = new JdbcOption(prefix, dto.getJdbc());
        } else {
            this.jdbcOption = new JdbcOption(prefix);
        }
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-resultType", this.resultType, ResultType.class);
        if (!result.hasValue("-resultType")) {
            return result;
        }
        final DataSourceType type = DataSourceType.valueOf(result.get("-resultType"));
        if (type == DataSourceType.table) {
            result.put("-op", this.operation == null ? DBConverter.Operation.CLEAN_INSERT : this.operation
                    , DBConverter.Operation.class);
            result.addComponent("jdbc", this.jdbcOption.toCommandLineArgs());
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

    public DataSetConsumerParam.Builder getParam() {
        return DataSetConsumerParam.builder()
                .setResultType(this.resultType)
                .setExportEmptyTable(this.exportEmptyTable)
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

    public File getResultDir() {
        return this.resultDir;
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

}
