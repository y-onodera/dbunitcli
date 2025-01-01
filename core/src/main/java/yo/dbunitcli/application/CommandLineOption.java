package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.ArgumentFilter;
import yo.dbunitcli.application.cli.ArgumentMapper;
import yo.dbunitcli.application.cli.DefaultArgumentFilter;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

public interface CommandLineOption<T extends CommandDto> extends Option {

    ArgumentFilter DEFAULT_COMMANDLINE_FILTER = new DefaultArgumentFilter("-P");
    ArgumentMapper DEFAULT_COMMANDLINE_MAPPER = new DefaultArgumentMapper();

    default String[] toArgs(final boolean containDefaultValue) {
        return this.toCommandLineArgs().toList(containDefaultValue).toArray(new String[0]);
    }

    T toDto();

    default ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    default ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter());
    }

    Parameter parameter();

}
