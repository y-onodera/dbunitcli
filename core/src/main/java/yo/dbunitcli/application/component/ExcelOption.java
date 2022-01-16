package yo.dbunitcli.application.component;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;

public class ExcelOption extends PrefixArgumentsParser implements ComparableDataSetParamOption{

    @Option(name = "-xlsxSchema", usage = "schema use read xlsx")
    private File xlsxSchemaSource;

    @Option(name = "-excelTable", usage = "SHEET or BOOK")
    private String excelTable = "SHEET";

    private XlsxSchema xlsxSchema;

    public ExcelOption(String prefix) {
        super(prefix);
    }

    @Override
    public void setUpComponent(CmdLineParser parser,String[] args) throws CmdLineException {
        try {
            this.xlsxSchema = new FromJsonXlsxSchemaBuilder().build(this.getXlsxSchemaSource());
        } catch (Exception e) {
            throw new CmdLineException(parser, e.getMessage(), e);
        }
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setXlsxSchema(this.xlsxSchema);
    }

    public XlsxSchema getXlsxSchema() {
        return xlsxSchema;
    }

    public File getXlsxSchemaSource() {
        return xlsxSchemaSource;
    }

    public String getExcelTable() {
        return excelTable;
    }
}
