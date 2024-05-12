package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.ArgumentFilter;
import yo.dbunitcli.application.cli.ArgumentMapper;
import yo.dbunitcli.application.cli.DefaultArgumentFilter;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.Option;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.util.Optional;
import java.util.function.UnaryOperator;

public abstract class CommandLineOption<T extends CommandDto> implements Option {

    public static ArgumentFilter DEFAULT_COMMANDLINE_FILTER = new DefaultArgumentFilter("-P");
    public static ArgumentMapper DEFAULT_COMMANDLINE_MAPPER = new DefaultArgumentMapper();
    private final Parameter parameter;
    private final DataSetConverterOption convertResult;
    private final String resultFile;

    public CommandLineOption(final String resultFile, final T dto, final Parameter param) {
        this.parameter = param;
        this.parameter.getMap().putAll(dto.getInputParam());
        this.resultFile = resultFile;
        this.convertResult = new DataSetConverterOption("result", dto.getConvertResult());
    }

    public String[] toArgs(final boolean containNoValue) {
        return this.toCommandLineArgs().toList(containNoValue).toArray(new String[0]);
    }

    public abstract T toDto();

    public Parameter getParameter() {
        return this.parameter;
    }

    public DataSetConverterOption getConvertResult() {
        return this.convertResult;
    }

    public IDataSetConverter converter() {
        return new DataSetConverterLoader().get(this.convertResult.getParam().build());
    }

    public IDataSetConverter converter(final UnaryOperator<DataSetConsumerParam.Builder> customizer) {
        return new DataSetConverterLoader().get(customizer.apply(this.convertResult.getParam()).build());
    }

    protected ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter);
    }

    protected ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    protected String getResultPath() {
        return Optional.ofNullable(this.convertResult.getResultPath())
                .filter(it -> !it.isEmpty())
                .orElse(this.resultFile);
    }

}
