package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;

import java.io.File;

public record ExcelOption(String prefix, File xlsxSchemaSource) implements ComparableDataSetParamOption {

    public ExcelOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, Strings.isNotEmpty(dto.getXlsxSchemaSource())
                ? FileResources.searchInOrderWorkspace(dto.getXlsxSchemaSource())
                : null);
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .putFile("-xlsxSchema", this.xlsxSchemaSource);
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
