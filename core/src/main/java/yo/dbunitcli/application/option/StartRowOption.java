package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record StartRowOption(
        String prefix,
        int startRow
) implements ComparableDataSetParamOption {

    public StartRowOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, Strings.isNotEmpty(dto.getStartRow()) ? Integer.parseInt(dto.getStartRow()) : 1);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setStartRow(this.startRow);
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.prefix)
                .put("-startRow", String.valueOf(this.startRow));
    }
}