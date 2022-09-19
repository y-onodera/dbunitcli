package yo.dbunitcli.application;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;
import yo.dbunitcli.application.argument.DataSetConsumerOption;
import yo.dbunitcli.application.argument.DefaultArgumentsParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.IDataSetConsumer;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.consumer.DataSetConsumerLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

abstract public class CommandLineOption extends DefaultArgumentsParser {

    @Option(name = "-P", handler = MapOptionHandler.class)
    private Map<String, String> inputParam = Maps.newHashMap();

    private final Parameter parameter;

    private final DataSetConsumerOption consumerOption = new DataSetConsumerOption("result");

    private String resultFile = "result";

    public CommandLineOption(Parameter param) {
        super("");
        this.parameter = param;
    }

    public void parse(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        String[] expandArgs = getExpandArgs(args, parser);
        if (args[0].startsWith("@")) {
            this.resultFile = new File(args[0].replace("@", "")).getName();
            this.resultFile = this.resultFile.substring(0, this.resultFile.lastIndexOf("."));
        }
        this.parseArgument(expandArgs);
    }

    public Parameter getParameter() {
        return this.parameter;
    }

    public DataSetConsumerOption getConsumerOption() {
        return this.consumerOption;
    }

    public IDataSetConsumer consumer() throws DataSetException {
        return new DataSetConsumerLoader().get(this.consumerOption.getParam().build());
    }

    public IDataSetConsumer consumer(File outputTo) throws DataSetException {
        return new DataSetConsumerLoader().get(this.consumerOption.getParam().setResultDir(outputTo).build());
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        this.parameter.getMap().putAll(this.inputParam);
    }

    protected ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(this.parameter);
    }

    protected ComparableDataSetParam.Builder getDataSetParamBuilder() {
        return ComparableDataSetParam.builder();
    }

    protected String[] getExpandArgs(String[] args, CmdLineParser parser) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method expand = CmdLineParser.class.getDeclaredMethod("expandAtFiles", String[].class);
        expand.setAccessible(true);
        return (String[]) expand.invoke(parser, (Object) args);
    }

    protected String getResultPath() {
        return Strings.isNullOrEmpty(this.consumerOption.getResultPath()) ? this.resultFile : this.consumerOption.getResultPath();
    }
}
