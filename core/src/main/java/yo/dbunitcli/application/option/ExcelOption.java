package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;

import java.io.File;

public record ExcelOption(String prefix, File xlsxSchemaSource) implements ComparableDataSetParamOption {

    public ExcelOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, Strings.isNotEmpty(dto.getXlsxSchemaSource())
                ? new File(dto.getXlsxSchemaSource())
                : null);
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.putFile("-xlsxSchema", this.xlsxSchemaSource);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        try {
            return builder.setXlsxSchema(new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource))
                    ;
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
