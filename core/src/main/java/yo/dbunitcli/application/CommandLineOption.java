package yo.dbunitcli.application;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.MapOptionHandler;
import yo.dbunitcli.application.component.DataSetWriteOption;
import yo.dbunitcli.application.component.PrefixArgumentsParser;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.IDataSetWriter;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.dataset.writer.DataSetWriterLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

abstract public class CommandLineOption extends PrefixArgumentsParser {

    @Option(name = "-P", handler = MapOptionHandler.class)
    private Map<String, String> inputParam = Maps.newHashMap();

    private final Parameter parameter;

    private DataSetWriteOption writeOption;

    private String resultFile = "result";

    public CommandLineOption(Parameter param) {
        super("");
        this.parameter = param;
    }

    public Parameter getParameter() {
        return this.parameter;
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

    public DataSetWriteOption getWriteOption() {
        return this.writeOption;
    }

    public IDataSetWriter writer() throws DataSetException {
        return this.writer(this.writeOption.getResultDir());
    }

    public IDataSetWriter writer(File outputTo) throws DataSetException {
        return new DataSetWriterLoader().get(this.writeOption.getParam().setResultDir(outputTo).build());
    }

    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        this.writeOption = new DataSetWriteOption("result", this.resultFile);
        this.writeOption.parseArgument(expandArgs);
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
}
