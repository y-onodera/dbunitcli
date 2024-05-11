package yo.dbunitcli.application.option;

import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class ExtensionOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final String extension;

    public ExtensionOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        this.extension = dto.getExtension();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setExtension(this.extension);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-extension", this.extension);
        return result;
    }


}
