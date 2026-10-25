package yo.dbunitcli.application.option;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.dto.DataSetLoadDto;
import yo.dbunitcli.dataset.ComparableDataSetParam;

import java.util.List;

public record TraverseOption(
        String prefix
        , String recursive
        , String regExclude
        , String regInclude
) implements ComparableDataSetParamOption {


    public TraverseOption(final String prefix, final DataSetLoadDto dto) {
        this(prefix
                , Strings.isNotEmpty(dto.getRecursive()) ? dto.getRecursive() : "false"
                , dto.getRegExclude()
                , dto.getRegInclude());
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix())
                .putFlag("-recursive", this.recursive)
                .put("-regInclude", this.regInclude)
                .put("-regExclude", this.regExclude);
    }

    @Override
    public List<Violation> validate() {
        return Option.concat(this.validateBoolean("-recursive", this.recursive)
                , this.validatePattern("-regInclude", this.regInclude)
                , this.validatePattern("-regExclude", this.regExclude));
    }

    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder.setRecursive(Boolean.parseBoolean(this.recursive))
                .setRegInclude(this.regInclude)
                .setRegExclude(this.regExclude);
    }

}
