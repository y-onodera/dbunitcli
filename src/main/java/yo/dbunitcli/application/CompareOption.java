package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSet;

import java.io.File;

public class CompareOption extends CommandLineOption {

    @Option(name = "-old", usage = "directory old files at", required = true)
    private File oldDir;

    @Option(name = "-oldsource", usage = "csv | csvq | xls | xlsx : default csv")
    private String oldsource = "csv";

    @Option(name = "-new", usage = "directory new files at", required = true)
    private File newDir;

    @Option(name = "-newsource", usage = "csv | csvq | xls | xlsx : default csv")
    private String newsource = "csv";

    @Option(name = "-expect", usage = "expected diff")
    private File expected;

    public File getOldDir() {
        return this.oldDir;
    }

    public File getNewDir() {
        return this.newDir;
    }

    public File getExpected() {
        return this.expected;
    }

    public ComparableDataSet oldDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.getOldDir(), this.getEncoding(), DataSourceType.fromString(this.oldsource), this.getExcludeColumns());
    }

    public ComparableDataSet newDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.getNewDir(), this.getEncoding(), DataSourceType.fromString(this.newsource), this.getExcludeColumns());
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        assertFileParameter(parser, this.newsource, this.newDir, "new");
        assertFileParameter(parser, this.oldsource, this.oldDir, "old");
    }

}
