package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class FixedOption implements ComparableDataSetParamOption {
    private final String prefix;
    private final String fixedLength;

    public FixedOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        this.fixedLength = dto.getFixedLength();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setFixedLength(this.fixedLength);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-fixedLength", this.fixedLength);
        return result;
    }

}
