package yo.dbunitcli.application;

import yo.dbunitcli.application.cli.ArgumentFilter;
import yo.dbunitcli.application.cli.ArgumentMapper;
import yo.dbunitcli.application.cli.DefaultArgumentFilter;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.OptionParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

abstract public class CommandLineOption<T extends CommandDto> implements OptionParser<T> {

    private final Parameter parameter;
    private final DataSetConverterOption converterOption = new DataSetConverterOption("result");
    private String resultFile = "result";

    public CommandLineOption(final Parameter param) {
        this.parameter = param;
    }

    public void parse(final String[] args) {
        final String[] expandArgs = this.getExpandArgs(args);
        if (args[0].startsWith("@")) {
            this.resultFile = new File(args[0].replace("@", "")).getName();
            this.resultFile = this.resultFile.substring(0, this.resultFile.lastIndexOf("."));
        }
        this.parseArgument(expandArgs);
    }

    public String[] getExpandArgs(final String[] args) {
        final List<String> result = new ArrayList<>();
        for (final String arg : args) {
            if (arg.startsWith("@")) {
                final File file = new File(arg.substring(1));
                if (!file.exists()) {
                    throw new AssertionError("file not exists :" + file.getPath());
                }
                try {
                    result.addAll(Files.readAllLines(file.toPath()));
                } catch (final IOException ex) {
                    throw new AssertionError("Failed to parse " + file, ex);
                }
            } else {
                result.add(arg);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * @param args option
     */
    public void parseArgument(final String[] args) {
        this.setUpComponent(this.toDto(args));
    }

    public List<String> toArgs(final boolean containNoValue) {
        return this.createOptionParam(new HashMap<>()).toList(containNoValue);
    }

    public abstract T toDto(String[] args);

    public Parameter getParameter() {
        return this.parameter;
    }

    public DataSetConverterOption getConverterOption() {
        return this.converterOption;
    }

    public IDataSetConverter converter() {
        return new DataSetConverterLoader().get(this.converterOption.getParam().build());
    }

    @Override
    public void setUpComponent(final T dto) {
        this.parameter.getMap().putAll(dto.getInputParam());
    }

    protected ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter);
    }

    protected ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    protected String getResultPath() {
        return Optional.ofNullable(this.converterOption.getResultPath())
                .filter(it -> !it.isEmpty())
                .orElse(this.resultFile);
    }

    protected ArgumentFilter getArgumentFilter() {
        return new DefaultArgumentFilter("-P");
    }

    protected ArgumentMapper getArgumentMapper() {
        return new DefaultArgumentMapper();
    }

}
