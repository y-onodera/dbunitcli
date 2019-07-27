package yo.dbunitcli.application;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ComparableDataSet;

import java.io.File;
import java.util.Map;

public class CompareOption extends CommandLineOption {

    @Option(name = "-old", usage = "directory old files at", required = true)
    private File oldDir;

    @Option(name = "-oldsource", usage = "table | sql | csv | csvq | xls | xlsx | reg : default csv")
    private String oldsource = "csv";

    @Option(name = "-new", usage = "directory new files at", required = true)
    private File newDir;

    @Option(name = "-newsource", usage = "table | sql | csv | csvq | xls | xlsx | reg : default csv")
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

    public CompareOption() {
        super(Maps.newHashMap());
    }

    public CompareOption(Map<String, Object> param) {
        super(param);
    }

    public ComparableDataSet oldDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.getOldDir())
                        .setSource(DataSourceType.fromString(this.oldsource))
                        .build()
        );
    }

    public ComparableDataSet newDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.getNewDir())
                        .setSource(DataSourceType.fromString(this.newsource))
                        .build()
        );
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        assertFileParameter(parser, this.newsource, this.newDir, "new");
        assertFileParameter(parser, this.oldsource, this.oldDir, "old");
    }

}
