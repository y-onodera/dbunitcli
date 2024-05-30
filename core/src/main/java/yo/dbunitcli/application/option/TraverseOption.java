package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public class TraverseOption implements ComparableDataSetParamOption {

    private final String prefix;
    private final boolean recursive;
    private final String regInclude;
    private final String regExclude;

    public TraverseOption(final String prefix, final DataSetLoadDto dto) {
        this.prefix = prefix;
        if (Strings.isNotEmpty(dto.getRecursive())) {
            this.recursive = Boolean.parseBoolean(dto.getRecursive());
        } else {
            this.recursive = false;
        }
        this.regExclude = dto.getRegExclude();
        this.regInclude = dto.getRegInclude();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs(this.getPrefix());
        result.put("-recursive", this.recursive);
        result.put("-regInclude", this.regInclude);
        result.put("-regExclude", this.regExclude);
        return result;
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setRecursive(this.recursive)
                .setRegInclude(this.regInclude)
                .setRegExclude(this.regExclude);
    }

}
