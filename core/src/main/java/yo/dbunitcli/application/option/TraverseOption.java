package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

public record TraverseOption(
        String prefix
        , boolean recursive
        , String regExclude
        , String regInclude
) implements ComparableDataSetParamOption {


    public TraverseOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix
                , Strings.isNotEmpty(dto.getRecursive()) && Boolean.parseBoolean(dto.getRecursive())
                , dto.getRegExclude()
                , dto.getRegInclude());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        return new CommandLineArgsBuilder(this.getPrefix())
                .put("-recursive", this.recursive)
                .put("-regInclude", this.regInclude)
                .put("-regExclude", this.regExclude);
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setRecursive(this.recursive)
                .setRegInclude(this.regInclude)
                .setRegExclude(this.regExclude);
    }

}
