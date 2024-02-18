package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.Parameter;

import java.util.Map;

public class ConvertOption extends CommandLineOption {

    private final DataSetLoadOption src = new DataSetLoadOption("");

    public ConvertOption() {
        this(Parameter.none());
    }

    public ConvertOption(final Parameter param) {
        super(param);
    }

    public void convertDataset() {
        this.getConverterOption().setResultPath(this.getResultPath());
        this.getComparableDataSetLoader().loadDataSet(this.src.getParam().setConverter(this.converter()).build());
    }

    @Override
    public void setUpComponent(final CommandLine.ParseResult parseResult, final String[] expandArgs) {
        super.setUpComponent(parseResult, expandArgs);
        this.src.parseArgument(expandArgs);
        this.getConverterOption().parseArgument(expandArgs);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putAll(this.src.createOptionParam(args));
        result.putAll(this.getConverterOption().createOptionParam(args));
        return result;
    }

    public ComparableDataSet targetDataSet() {
        return this.getComparableDataSetLoader().loadDataSet(this.src.getParam().build());
    }

}
