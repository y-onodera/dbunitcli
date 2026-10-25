package yo.dbunitcli.application;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

public interface CommandLineOption<T extends CommandDto> extends Option {

    ArgumentFilter ARGUMENT_FILTER = new DefaultArgumentFilter("-P");
    ArgumentFunction ARGUMENT_FUNCTION = new DefaultArgumentFunction();

    default String[] toArgs(final boolean containDefaultValue) {
        return this.toParameters().toList(containDefaultValue).toArray(new String[0]);
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
