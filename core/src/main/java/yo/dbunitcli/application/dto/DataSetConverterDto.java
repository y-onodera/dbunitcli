package yo.dbunitcli.application.dto;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DBConverter;

import java.util.stream.Stream;

public class DataSetConverterDto implements CompositeDto {
    @CommandLine.Option(names = "-result", description = "directory result files at")
    private String resultDir;
    @CommandLine.Option(names = "-resultType")
    private ResultType resultType;
    @CommandLine.Option(names = "-exportEmptyTable", description = "if true then empty table is not export")
    private String exportEmptyTable;
    @CommandLine.Option(names = "-resultPath", description = "result file relative path from -result=dir.")
    private String resultPath;
    @CommandLine.Option(names = "-outputEncoding", description = "output csv file encoding")
    private String outputEncoding;
    @CommandLine.Option(names = "-op", description = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT")
    private DBConverter.Operation operation;
    @CommandLine.Option(names = "-excelTable", description = "SHEET or BOOK")
    private String excelTable;

    private JdbcDto jdbc = new JdbcDto();

    @Override
    public Stream<Object> dto() {
        return Stream.of(this, this.jdbc);
    }

    public String getResultDir() {
        return this.resultDir;
    }

    public void setResultDir(final String resultDir) {
        this.resultDir = resultDir;
    }

    public ResultType getResultType() {
        return this.resultType;
    }

    public void setResultType(final ResultType resultType) {
        this.resultType = resultType;
    }

    public String getExportEmptyTable() {
        return this.exportEmptyTable;
    }

    public void setExportEmptyTable(final String exportEmptyTable) {
        this.exportEmptyTable = exportEmptyTable;
    }

    public String getResultPath() {
        return this.resultPath;
    }

    public void setResultPath(final String resultPath) {
        this.resultPath = resultPath;
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

    public void setOutputEncoding(final String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public DBConverter.Operation getOperation() {
        return this.operation;
    }

    public void setOperation(final DBConverter.Operation operation) {
        this.operation = operation;
    }

    public String getExcelTable() {
        return this.excelTable;
    }

    public void setExcelTable(final String excelTable) {
        this.excelTable = excelTable;
    }

    public JdbcDto getJdbc() {
        return this.jdbc;
    }

    public void setJdbc(final JdbcDto jdbc) {
        this.jdbc = jdbc;
    }
}
