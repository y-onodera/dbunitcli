package yo.dbunitcli.application;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;
import yo.dbunitcli.application.argument.DataSetConverterOption;
import yo.dbunitcli.application.argument.DefaultArgumentsParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

abstract public class CommandLineOption extends DefaultArgumentsParser {

    @Option(name = "-P", handler = MapOptionHandler.class)
    private Map<String, String> inputParam = new HashMap<>();

    private final Parameter parameter;

    private final DataSetConverterOption converterOption = new DataSetConverterOption("result");

    private String resultFile = "result";

    public CommandLineOption(final Parameter param) {
        super("");
        this.parameter = param;
    }

    public void parse(final String[] args) {
        final CmdLineParser parser = new CmdLineParser(this);
        final String[] expandArgs = this.getExpandArgs(args, parser);
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
    public void setUpComponent(final CmdLineParser parser, final String[] expandArgs) throws CmdLineException {
        this.parameter.getMap().putAll(this.inputParam);
    }

    protected ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter);
    }

    protected ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    protected String[] getExpandArgs(final String[] args, final CmdLineParser parser) {
        try {
            final Method expand = CmdLineParser.class.getDeclaredMethod("expandAtFiles", String[].class);
            expand.setAccessible(true);
            return (String[]) expand.invoke(parser, (Object) args);
        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    protected String getResultPath() {
        return Optional.ofNullable(this.converterOption.getResultPath())
                .filter(it -> !it.isEmpty())
                .orElse(this.resultFile);
    }
}
