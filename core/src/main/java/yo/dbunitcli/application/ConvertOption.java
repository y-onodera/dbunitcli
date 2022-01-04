package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.Parameter;

import java.io.File;

public class ConvertOption extends CommandLineOption {

    @Option(name = "-src", usage = "export target", required = true)
    private File src;

    @Option(name = "-srcType", usage = "table | sql | csv | csvq | xls | xlsx | fixed | reg | file | dir")
    private String srcType = "csv";

    public ConvertOption() {
        this(Parameter.none());
    }

    public ConvertOption(Parameter param) {
        super(param);
    }

    @Override
    public void parse(String[] args) throws Exception {
        super.parse(args);
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.src)
                        .setSource(DataSourceType.fromString(this.srcType))
                        .build()
        );
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.srcType, this.src, "src");
    }

}
