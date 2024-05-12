package yo.dbunitcli.application.option;

import yo.dbunitcli.dataset.ComparableDataSetParam;

public class NoneOption implements ComparableDataSetParamOption {
    @Override
    public ComparableDataSetParam.Builder populate(final ComparableDataSetParam.Builder builder) {
        return builder;
    }

    @Override
    public CommandLineArgs toCommandLineArgs() {
        return new CommandLineArgs(this.getPrefix());
    }
}
