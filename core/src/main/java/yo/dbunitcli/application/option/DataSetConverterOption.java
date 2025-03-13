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
        , JdbcOption jdbcOption
        , DBConverter.Operation operation
        , String resultDir
        , String resultPath
        , boolean exportEmptyTable
        , boolean skipHeader
        , String outputEncoding
        , String excelTable
) implements Option {

    private static ResultType resultType(final DataSetConverterDto dto) {
        return dto.getResultType() != null ? dto.getResultType() : ResultType.csv;
    }

    public DataSetConverterOption(final String prefix, final DataSetConverterDto dto) {
        this(prefix
                , DataSetConverterOption.resultType(dto)
                , DataSetConverterOption.resultType(dto) == ResultType.table ? new JdbcOption(prefix, dto.getJdbc()) : new JdbcOption(prefix)
                , dto.getOperation()
                , dto.getResultDir()
                , dto.getResultPath()
                , Strings.isEmpty(dto.getExportEmptyTable()) || Boolean.parseBoolean(dto.getExportEmptyTable())
                , Strings.isEmpty(dto.getExportHeader()) || Boolean.parseBoolean(dto.getExportHeader())
                , Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8"
                , Strings.isNotEmpty(dto.getExcelTable()) ? dto.getExcelTable() : "SHEET"
        );
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        final CommandLineArgsBuilder result = new CommandLineArgsBuilder(this.getPrefix());
        result.put("-resultType", this.resultType, ResultType.class);
        if (this.resultType == null) {
            return result;
        }
        final DataSourceType type = DataSourceType.valueOf(this.resultType.name());
        if (type == DataSourceType.table) {
            result.put("-op", this.operation == null ? DBConverter.Operation.CLEAN_INSERT : this.operation
                            , DBConverter.Operation.class)
                    .addComponent("jdbc", this.jdbcOption.toCommandLineArgs());
        } else {
            result.putDir("-result", this.resultDir, BaseDir.RESULT)
                    .put("-resultPath", this.resultPath)
                    .put("-exportEmptyTable", this.exportEmptyTable)
                    .put("-exportHeader", this.skipHeader);
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
                .setOperation(this.operation)
                .setDatabaseConnectionLoader(this.jdbcOption.getDatabaseConnectionLoader())
                .setResultDir(this.getResultDir())
                .setResultPath(this.resultPath)
                .setExportEmptyTable(this.exportEmptyTable)
                .setSkipHeader(this.skipHeader)
                .setOutputEncoding(this.outputEncoding)
                .setExcelTable(this.excelTable)
                ;
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir);
    }

}
