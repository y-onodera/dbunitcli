package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.FileResources;

import java.io.File;

public record DataSetConverterOption(
        String prefix
        , ResultType resultType
        , File resultDir
        , String resultPath
        , boolean exportEmptyTable
        , JdbcOption jdbcOption
        , DBConverter.Operation operation
        , String outputEncoding
        , String excelTable
) implements Option {

    private static ResultType resultType(final DataSetConverterDto dto) {
        return dto.getResultType() != null ? dto.getResultType() : ResultType.csv;
    }

    public DataSetConverterOption(final String prefix, final DataSetConverterDto dto) {
        this(prefix
                , DataSetConverterOption.resultType(dto)
                , FileResources.resultDir(dto.getResultDir())
                , dto.getResultPath()
                , !Strings.isNotEmpty(dto.getExportEmptyTable()) || Boolean.parseBoolean(dto.getExportEmptyTable())
                , DataSetConverterOption.resultType(dto) == ResultType.table ? new JdbcOption(prefix, dto.getJdbc()) : new JdbcOption(prefix)
                , dto.getOperation()
                , Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8"
                , Strings.isNotEmpty(dto.getExcelTable()) ? dto.getExcelTable() : "SHEET"
        );
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

    public DataSetConverterParam.Builder getParam() {
        return DataSetConverterParam.builder()
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

}
