package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;

public record ExcelOption(String prefix, String xlsxSchemaSource) implements ComparableDataSetParamOption {

    public ExcelOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getXlsxSchemaSource());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .putFile("-xlsxSchema", this.xlsxSchemaSource, BaseDir.XLSX_SCHEMA);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        try {
            return builder.setXlsxSchema(new FromJsonXlsxSchemaBuilder().build(FileResources.searchXlsxSchema(this.xlsxSchemaSource)))
                    ;
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
