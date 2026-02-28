package yo.dbunitcli.application.option;

import yo.dbunitcli.dataset.ComparableDataSetParam;

public record NoneOption() implements ComparableDataSetParamOption {
    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder;
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        return new ParametersBuilder(this.getPrefix());
    }
}
