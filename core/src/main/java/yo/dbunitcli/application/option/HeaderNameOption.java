package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
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
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-headerName", this.headerName);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        if (Strings.isEmpty(this.headerName)) {
            return builder;
        }
        return builder.setHeaderName(this.headerName);
    }

}
