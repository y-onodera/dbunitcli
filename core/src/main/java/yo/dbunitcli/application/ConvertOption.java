package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.component.DataSetLoadOption;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;

public class ConvertOption extends CommandLineOption {

    private DataSetLoadOption src = new DataSetLoadOption("src");

    public ConvertOption() {
        this(Parameter.none());
    }

    public ConvertOption(Parameter param) {
        super(param);
    }

    @Override
    protected void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.src.parseArgument(expandArgs);
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.src.getParam().build());
    }

}
