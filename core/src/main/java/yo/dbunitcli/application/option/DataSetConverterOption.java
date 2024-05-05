package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DBConverter;

import java.io.File;
import java.util.Map;

public class DataSetConverterOption implements OptionParser<DataSetConverterDto> {

    private final String prefix;
    private final JdbcOption jdbcOption;
    private final DataSetConsumerParam.Builder builder;
    private File resultDir = new File(".");
    private ResultType resultType = ResultType.csv;
    private String exportEmptyTable = "true";
    private String resultPath;
    private String outputEncoding = "UTF-8";
    private DBConverter.Operation operation;
    private String excelTable = "SHEET";

    public DataSetConverterOption(final String prefix) {
        this.prefix = prefix;
        this.jdbcOption = new JdbcOption(prefix);
        this.builder = DataSetConsumerParam.builder();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
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
    public void setUpComponent(final DataSetConverterDto dto) {
        if (dto.getResultType() != null) {
            this.resultType = dto.getResultType();
        }
        if (Strings.isNotEmpty(dto.getResultDir())) {
            this.resultDir = new File(dto.getResultDir());
        }
        if (Strings.isNotEmpty(dto.getResultPath())) {
            this.resultPath = dto.getResultPath();
        }
        if (Strings.isNotEmpty(dto.getExportEmptyTable())) {
            this.exportEmptyTable = dto.getExportEmptyTable();
        }
        if (Strings.isNotEmpty(dto.getOutputEncoding())) {
            this.outputEncoding = dto.getOutputEncoding();
        }
        this.operation = dto.getOperation();
        if (Strings.isNotEmpty(dto.getExcelTable())) {
            this.excelTable = dto.getExcelTable();
        }
        if (this.resultType == ResultType.table) {
            this.jdbcOption.setUpComponent(dto.getJdbc());
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
