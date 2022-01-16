package yo.dbunitcli.application.argument;

import com.google.common.base.Strings;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.DataSetWriterParam;

import java.io.File;

public class DataSetWriteOption extends PrefixArgumentsParser {

    @Option(name = "-result", usage = "directory result files at")
    private File resultDir = new File(".");

    @Option(name = "-resultType", usage = "csv | xls | xlsx | table ")
    private String resultType = "csv";

    @Option(name = "-exportEmptyTable", usage = "if true then empty table is not export")
    private String exportEmptyTable = "true";

    @Option(name = "-resultPath", usage = "result file relative path from -result=dir.")
    private String resultPath;

    @Option(name = "-outputEncoding", usage = "output csv file encoding")
    private String outputEncoding = "UTF-8";

    private final String defaultResultPath;

    private final JdbcOption jdbcOption;

    private final ExcelOption excelOption;

    private final DataSetWriterParam.Builder builder;

    public DataSetWriteOption(String prefix, String resultFile) {
        super(prefix);
        this.defaultResultPath = resultFile;
        this.jdbcOption = new JdbcOption(prefix);
        this.excelOption = new ExcelOption(prefix);
        this.builder = DataSetWriterParam.builder();
    }

    @Override
    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.jdbcOption.parseArgument(expandArgs);
        this.excelOption.parseArgument(expandArgs);
        this.builder.setResultType(this.resultType)
                .setOutputEncoding(this.outputEncoding)
                .setExportEmptyTable(Boolean.parseBoolean(this.exportEmptyTable))
                .setOperation(this.jdbcOption.getOperation())
                .setDatabaseConnectionLoader(this.jdbcOption.getDatabaseConnectionLoader())
                .setExcelTable(this.excelOption.getExcelTable());
    }

    public DataSetWriterParam.Builder getParam() {
        return builder;
    }

    public String getResultType() {
        return resultType;
    }

    public JdbcOption getJdbcOption() {
        return this.jdbcOption;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public String getResultPath() {
        return Strings.isNullOrEmpty(this.resultPath) ? this.defaultResultPath : this.resultPath;
    }

    public File getResultFile() {
        return new File(this.resultDir, this.getResultPath());
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

}
