package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.argument.ArgumentFilter;
import yo.dbunitcli.application.argument.DataSetConverterOption;
import yo.dbunitcli.application.argument.DefaultArgumentFilter;
import yo.dbunitcli.application.argument.DefaultArgumentsParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

abstract public class CommandLineOption extends DefaultArgumentsParser {

    private final Parameter parameter;
    private final DataSetConverterOption converterOption = new DataSetConverterOption("result");
    @CommandLine.Option(names = "-P")
    private Map<String, String> inputParam = new HashMap<>();
    private String resultFile = "result";

    public CommandLineOption(final Parameter param) {
        super("");
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
    public ArgumentFilter getArgumentFilter() {
        return new DefaultArgumentFilter("-P");
    }

    @Override
    public void setUpComponent(final CommandLine.ParseResult parseResult, final String[] expandArgs) {
        this.parameter.getMap().putAll(this.inputParam);
    }

    protected ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter);
    }

    protected ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    protected String[] getExpandArgs(final String[] args) {
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

    protected String getResultPath() {
        return Optional.ofNullable(this.converterOption.getResultPath())
                .filter(it -> !it.isEmpty())
                .orElse(this.resultFile);
    }

}
