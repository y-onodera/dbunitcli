package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSet;

import java.io.File;

public class ExportOption extends CommandLineOption {

    @Option(name = "-src", usage = "export target", required = true)
    private File src;

    @Option(name = "-srcType", usage = "table | sql | csv | csvq | xls | xlsx : default csv")
    private String srcType = "csv";

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.srcType, this.src, "src");
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.src, this.getEncoding(), DataSourceType.fromString(this.srcType), this.getExcludeColumns());
    }

}
