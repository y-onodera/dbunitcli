package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class RecursiveOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final boolean recursive;

    public RecursiveOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        if (Strings.isNotEmpty(dto.getRecursive())) {
            this.recursive = Boolean.parseBoolean(dto.getRecursive());
        } else {
            this.recursive = true;
        }
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setRecursive(this.recursive);
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-recursive", this.recursive);
        return result;
    }

}
