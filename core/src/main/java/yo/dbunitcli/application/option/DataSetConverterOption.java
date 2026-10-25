package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetConverterDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.XlsConverter;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.util.List;

public record DataSetConverterOption(
        String prefix
        , ResultType resultType
        , JdbcOption jdbcOption
        , DbOperation operation
        , String resultDir
        , String resultPath
        , String exportEmptyTable
        , String skipHeader
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
                , Strings.isNotEmpty(dto.getExportEmptyTable()) ? dto.getExportEmptyTable() : "true"
                , Strings.isNotEmpty(dto.getExportHeader()) ? dto.getExportHeader() : "true"
                , Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8"
                , Strings.isNotEmpty(dto.getExcelTable()) ? dto.getExcelTable() : "SHEET"
                , dto.getFormat()
                , dto.getExtension()
                , dto.getFixedColumnDefFile()
                , Strings.isNotEmpty(dto.getFixedLengthType()) ? dto.getFixedLengthType() : "char"
                , DataSetConverterOption.parallel(dto.getParallel())
        );
    }

    /**
     * -parallelの値を整える。未指定は空文字（未指定時の並列数はコマンドごとに決める。convert/compare/generateはCPUコア数で自動（作業スレッドの中では逐次）、run/parameterizeは逐次）。
     * 値の検証は{@link Option#validateParallel(String)}で行う
     */
    public static String parallel(final String parallel) {
        return Strings.isEmpty(parallel) ? "" : parallel.trim();
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
     * -resultは出力先のため存在確認しない
     */
    @Override
    public List<Violation> validate() {
        return Option.concat(this.validateResultType()
                , this.resultType == ResultType.table ? List.of() : Option.concat(
                        this.validateBoolean("-exportEmptyTable", this.exportEmptyTable)
                        , this.validateBoolean("-exportHeader", this.skipHeader))
                , this.validateParallel(this.parallel));
    }

    private List<Violation> validateResultType() {
        return switch (this.resultType) {
            case table -> this.jdbcOption.validate();
            case xls, xlsx -> this.validateOneOf("-excelTable", this.excelTable, XlsConverter.TableExportType.values());
            case fixed -> Option.concat(this.validateEncoding("-outputEncoding", this.outputEncoding)
                    , this.validateFileExists("-fixedColumnDef", this.fixedColumnDefFile, BaseDir.FIXED_COLUMN_DEF)
                    , this.validateOneOfIgnoreCase("-fixedLengthType", this.fixedLengthType
                            , ComparableDataSetParam.LengthType.values()));
            case format -> Option.concat(this.validateEncoding("-outputEncoding", this.outputEncoding)
                    , this.validateRequired("-format", this.format));
            case null -> List.of();
            default -> this.validateEncoding("-outputEncoding", this.outputEncoding);
        };
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
                  .putFlag("-exportEmptyTable", this.exportEmptyTable)
                  .putFlag("-exportHeader", this.skipHeader);
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
     * 出力側が並列書込に対応するか。xls/xlsxは1ブックにまとめるSHEETでは不可。
     * excelTableは環境変数で渡せる自由入力のため、SHEETと確定しているときだけ不可とする（実行時はconverterが判定する）
     */
    public boolean isParallelizable() {
        return (this.resultType != ResultType.xls && this.resultType != ResultType.xlsx)
                || !XlsConverter.TableExportType.SHEET.name().equals(this.excelTable);
    }

    public DataSetConverterParam.Builder getParam() {
        return DataSetConverterParam.builder()
                                    .setResultType(this.resultType)
                                    .setOperation(this.operation)
                                    .setDatabaseConnectionLoader(this.jdbcOption.getDatabaseConnectionLoader())
                                    .setResultDir(this.getResultDir())
                                    .setResultPath(this.resultPath)
                                    .setExportEmptyTable(Boolean.parseBoolean(this.exportEmptyTable))
                                    .setSkipHeader(Boolean.parseBoolean(this.skipHeader))
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
        return DataSetConverterOption.parallelism(this.parallel);
    }

    /**
     * @param parallel validateParallel(String)で検証した値
     * @return ソースの並列読込数。未指定ならnull（並列化できればCPUコア数で自動）
     */
    public static Integer parallelism(final String parallel) {
        return Strings.isEmpty(parallel) ? null : Integer.valueOf(parallel);
    }

    /**
     * @param parallel  validateParallel(String)で検証した値
     * @param whenEmpty 未指定のときの並列数
     */
    public static int parallelism(final String parallel, final int whenEmpty) {
        return Strings.isEmpty(parallel) ? whenEmpty : Integer.parseInt(parallel);
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir);
    }

}
