package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.Parameter;

import java.util.Map;

public class ConvertOption extends CommandLineOption {

    private DataSetLoadOption src = new DataSetLoadOption("");

    public ConvertOption() {
        this(Parameter.none());
    }

    public ConvertOption(Parameter param) {
        super(param);
    }

    public void convertDataset() throws DataSetException {
        this.getConverterOption().setResultPath(this.getResultPath());
        this.getComparableDataSetLoader().loadDataSet(this.src.getParam().setConsumer(this.converter()).build());
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.src.parseArgument(expandArgs);
        this.getConverterOption().parseArgument(expandArgs);
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putAll(this.src.createOptionParam(args));
        result.putAll(this.getConverterOption().createOptionParam(args));
        return result;
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.src.getParam().build());
    }

}
