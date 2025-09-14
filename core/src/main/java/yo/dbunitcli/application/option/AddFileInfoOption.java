package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record AddFileInfoOption(
        String prefix
        , boolean addFileInfo
) implements ComparableDataSetParamOption {

    public AddFileInfoOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix, dto.getAddFileInfo());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-addFileInfo", this.addFileInfo);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setAddFileInfo(this.addFileInfo);
    }

}