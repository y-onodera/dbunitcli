package yo.dbunitcli.application.argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;
import java.util.Map;

public class ExcelOption extends PrefixArgumentsParser implements ComparableDataSetParamOption {

    @Option(name = "-xlsxSchema", usage = "schema use read xlsx")
    private File xlsxSchemaSource;

    private XlsxSchema xlsxSchema;

    public ExcelOption(String prefix) {
        super(prefix);
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] args) throws CmdLineException {
        try {
            this.xlsxSchema = new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource);
        } catch (Exception e) {
            throw new CmdLineException(parser, e.getMessage(), e);
        }
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = super.expandOption(args);
        result.put("-xlsxSchema", this.xlsxSchemaSource);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(ComparableDataSetParam.Builder builder) {
        return builder.setXlsxSchema(this.xlsxSchema);
    }

}
