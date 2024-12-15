package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record FixedOption(
        String prefix
        , String fixedLength
) implements ComparableDataSetParamOption {

    public FixedOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getFixedLength());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-fixedLength", this.fixedLength);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength);
    }

}
