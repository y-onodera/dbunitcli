package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;
import java.util.Map;

public class ExcelOption implements ComparableDataSetParamOption {
    private final String prefix;

    private File xlsxSchemaSource;

    private XlsxSchema xlsxSchema;

    public ExcelOption(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setXlsxSchema(this.xlsxSchema);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-xlsxSchema", this.xlsxSchemaSource);
        return result;
    }

    @Override
    public void setUpComponent(final DataSetLoadDto dto) {
        if (Strings.isNotEmpty(dto.getXlsxSchemaSource())) {
            this.xlsxSchemaSource = new File(dto.getXlsxSchemaSource());
        }
        try {
            this.xlsxSchema = new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

}
