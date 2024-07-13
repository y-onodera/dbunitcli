package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.ArgumentFilter;
import yo.dbunitcli.application.cli.ArgumentMapper;
import yo.dbunitcli.application.cli.DefaultArgumentFilter;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface CommandLineOption<T extends CommandDto> extends Option {

    ArgumentFilter DEFAULT_COMMANDLINE_FILTER = new DefaultArgumentFilter("-P");
    ArgumentMapper DEFAULT_COMMANDLINE_MAPPER = new DefaultArgumentMapper();

    default String[] toArgs(final boolean containNoValue) {
        return this.toCommandLineArgs().toList(containNoValue).toArray(new String[0]);
    }

    T toDto();

    default Parameter getParameter() {
        return this.base().parameter();
    }

    default DataSetConverterOption getConvertResult() {
        return this.base().convertResult();
    }

    default IDataSetConverter converter() {
        return this.base().converter();
    }

    default IDataSetConverter converter(final UnaryOperator<DataSetConverterParam.Builder> customizer) {
        return this.base().converter(customizer);
    }

    default ComparableDataSetLoader getComparableDataSetLoader() {
        return this.base().getComparableDataSetLoader();
    }

    default ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    default String getResultPath() {
        return this.base().getResultPath();
    }

    BaseOption base();

    record BaseOption(Parameter parameter
            , String resultFile
            , DataSetConverterOption convertResult) {
        BaseOption(final String resultFile, final CommandDto dto, final Parameter param) {
            this(param.addAll(dto.getInputParam())
                    , resultFile
                    , new DataSetConverterOption("result", dto.getConvertResult()));
        }

        IDataSetConverter converter() {
            return new DataSetConverterLoader().get(this.convertResult.getParam().build());
        }

        IDataSetConverter converter(final UnaryOperator<DataSetConverterParam.Builder> customizer) {
            return new DataSetConverterLoader().get(customizer.apply(this.convertResult.getParam()).build());
        }

        ComparableDataSetLoader getComparableDataSetLoader() {
            return new ComparableDataSetLoader(this.parameter);
        }

        String getResultPath() {
            return Optional.ofNullable(this.convertResult.resultPath())
                    .filter(it -> !it.isEmpty())
                    .orElse(this.resultFile);
        }
    }

}
