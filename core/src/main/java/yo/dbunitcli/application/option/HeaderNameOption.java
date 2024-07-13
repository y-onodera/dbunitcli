package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record HeaderNameOption(String prefix, String headerName) implements ComparableDataSetParamOption {

    public HeaderNameOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getHeaderName());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-headerName", this.headerName);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setHeaderName(this.headerName);
    }


}
