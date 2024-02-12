package yo.dbunitcli.application.argument;

import yo.dbunitcli.dataset.ComparableDataSetParam;

public class NoneOption implements ComparableDataSetParamOption {
    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder;
    }

    @Override
    public void parseArgument(final String[] args) {
        // nothing to do
    }
}
