package yo.dbunitcli.application.argument;

import picocli.CommandLine;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;
import java.util.Map;

public class ExcelOption extends DefaultArgumentsParser implements ComparableDataSetParamOption {

    @CommandLine.Option(names = "-xlsxSchema", description = "schema use read xlsx")
    private File xlsxSchemaSource;

    private XlsxSchema xlsxSchema;

    public ExcelOption(final String prefix) {
        super(prefix);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-xlsxSchema", this.xlsxSchemaSource);
        return result;
    }

    @Override
    public void setUpComponent(final String[] args) {
        try {
            this.xlsxSchema = new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setXlsxSchema(this.xlsxSchema);
    }

}
