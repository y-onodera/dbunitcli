package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class HeaderNameOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final String headerName;

    public HeaderNameOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        this.headerName = dto.getHeaderName();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setHeaderName(this.headerName);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-headerName", this.headerName);
        return result;
    }


}
