package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.resource.poi.FromJsonXlsxSchemaBuilder;

import java.io.File;

public class ExcelOption implements ComparableDataSetParamOption {
    private final String prefix;
    private final File xlsxSchemaSource;
    private final String regSheetInclude;
    private final String regSheetExclude;

    public ExcelOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        if (Strings.isNotEmpty(dto.getXlsxSchemaSource())) {
            this.xlsxSchemaSource = new File(dto.getXlsxSchemaSource());
        } else {
            this.xlsxSchemaSource = null;
        }
        this.regSheetInclude = dto.getRegSheetInclude();
        this.regSheetExclude = dto.getRegSheetExclude();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.putFile("-xlsxSchema", this.xlsxSchemaSource);
        result.put("-regSheetInclude", this.regSheetInclude);
        result.put("-regSheetExclude", this.regSheetExclude);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        try {
            return builder.setXlsxSchema(new FromJsonXlsxSchemaBuilder().build(this.xlsxSchemaSource))
                    .setRegSheetInclude(this.regSheetInclude)
                    .setRegSheetExclude(this.regSheetExclude)
                    ;
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

}
