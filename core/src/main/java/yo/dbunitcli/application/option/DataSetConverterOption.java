package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.XlsConverter;
import yo.dbunitcli.resource.FileResources;

import java.io.File;

public record DataSetConverterOption(
        String prefix
        , ResultType resultType
        , JdbcOption jdbcOption
        , DbOperation operation
        , String resultDir
        , String resultPath
        , boolean exportEmptyTable
        , boolean skipHeader
        , String outputEncoding
        , String excelTable
        , String format
        , String extension
        , String fixedColumnDefFile
        , String fixedLengthType
        , String parallel
) implements Option {

    public DataSetConverterOption(final String prefix, final DataSetConverterDto dto) {
        this(prefix
                , DataSetConverterOption.resultType(dto)
                , DataSetConverterOption.resultType(dto) == ResultType.table ? new JdbcOption(prefix, dto.getJdbc()) :
                     new JdbcOption(prefix)
                , dto.getOperation()
                , dto.getResultDir()
                , dto.getResultPath()
                , Strings.isEmpty(dto.getExportEmptyTable()) || Boolean.parseBoolean(dto.getExportEmptyTable())
                , Strings.isEmpty(dto.getExportHeader()) || Boolean.parseBoolean(dto.getExportHeader())
                , Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8"
                , Strings.isNotEmpty(dto.getExcelTable()) ? dto.getExcelTable() : "SHEET"
                , dto.getFormat()
                , dto.getExtension()
                , dto.getFixedColumnDefFile()
                , Strings.isNotEmpty(dto.getFixedLengthType()) ? dto.getFixedLengthType() : "char"
                , DataSetConverterOption.parallel(dto)
        );
    }

    private static String parallel(final DataSetConverterDto dto) {
        if (Strings.isEmpty(dto.getParallel())) {
            return "";
        }
        try {
            if (Integer.parseInt(dto.getParallel().trim()) < 1) {
                throw new IllegalArgumentException("-parallel must be 1 or more: " + dto.getParallel());
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("-parallel must be a number: " + dto.getParallel(), e);
        }
        return dto.getParallel().trim();
    }

    private static ResultType resultType(final DataSetConverterDto dto) {
        return dto.getResultType() != null ? dto.getResultType() : ResultType.csv;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return this.toParametersBuilder(false);
    }

    /**
     * @param showParallel -parallel を含めるか（入力側も並列化できるかは呼び出し側が判定する）
     */
    public ParametersBuilder toParametersBuilder(final boolean showParallel) {
        final ParametersBuilder result = new ParametersBuilder(this.getPrefix());
        result.put("-resultType", this.resultType, ResultType.class);
        if (this.resultType == null) {
            return result;
        }
        final ResultType type = ResultType.valueOf(this.resultType.name());
        if (type == ResultType.table) {
            result.put("-op", this.operation == null ? DbOperation.CLEAN_INSERT : this.operation
                          , DbOperation.class)
                  .addComponent("jdbc", this.jdbcOption.toParameters());
        } else {
            result.putDir("-result", this.resultDir, BaseDir.RESULT)
                  .put("-resultPath", this.resultPath)
                  .put("-exportEmptyTable", this.exportEmptyTable)
                  .put("-exportHeader", this.skipHeader);
            if (type == ResultType.csv) {
                result.put("-outputEncoding", this.outputEncoding);
                result.put("-outputExtension", this.extension);
            } else if (type == ResultType.format) {
                result.put("-outputEncoding", this.outputEncoding);
                result.put("-format", this.format);
                result.put("-outputExtension", this.extension);
            } else if (type == ResultType.fixed) {
                result.put("-outputEncoding", this.outputEncoding);
                result.put("-outputExtension", this.extension);
                result.putFile("-fixedColumnDef", this.fixedColumnDefFile, BaseDir.FIXED_COLUMN_DEF);
                result.put("-fixedLengthType", this.fixedLengthType);
            } else {
                result.put("-excelTable", this.excelTable);
            }
        }
        if (showParallel) {
            result.put("-parallel", this.parallel);
        }
        return result;
    }

    /**
     * 出力側が並列書込に対応するか（xls/xlsxは1ブックにまとめるSHEETでは不可）
     */
    public boolean isParallelizable() {
        return (this.resultType != ResultType.xls && this.resultType != ResultType.xlsx)
                || XlsConverter.TableExportType.BOOK.name().equals(this.excelTable);
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
                                    .setFormat(this.format)
                                    .setExtension(this.extension)
                                    .setFixedColumnDefFile(this.fixedColumnDefFile)
                                    .setFixedLengthType(this.fixedLengthType)
                ;
    }

    /**
     * @return ソースの並列読込数。未指定ならnull（並列化できればCPUコア数で自動）
     */
    public Integer parallelism() {
        return Strings.isEmpty(this.parallel) ? null : Integer.valueOf(this.parallel);
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir);
    }

}
