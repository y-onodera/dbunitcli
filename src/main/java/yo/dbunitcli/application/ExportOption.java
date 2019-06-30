package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSet;

import java.io.File;

public class ExportOption extends CommandLineOption {

    @Option(name = "-dir", usage = "directory files at", required = true)
    private File dir;

    @Option(name = "-source", usage = "csv | csvq | xls | xlsx : default csv")
    private String source = "csv";

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.source, this.dir, "dir");
    }

    public ComparableDataSet targetDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.dir, this.getEncoding(), DataSourceType.fromString(this.source), this.getExcludeColumns());
    }

}
