package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.setting.FromJsonColumnSettingsBuilder;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.writer.IDataSetWriter;

import java.io.File;
import java.io.IOException;

public class CompareOption extends CommandLineOption {

    @Option(name = "-old", usage = "directory old files at", required = true)
    private File oldDir;

    @Option(name = "-oldsource", usage = "table | sql | csv | csvq | xls | xlsx | reg ")
    private String oldsource = "csv";

    @Option(name = "-new", usage = "directory new files at", required = true)
    private File newDir;

    @Option(name = "-newsource", usage = "table | sql | csv | csvq | xls | xlsx | reg ")
    private String newsource = "csv";

    @Option(name = "-expect", usage = "expected diff")
    private File expected;

    @Option(name = "-expectDetail", usage = "file define expected diff comparison settings")
    private File expectDetail;

    private ColumnSettings expectDetailSettings;

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
        super(Parameter.none());
    }

    public CompareOption(Parameter param) {
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

    public ComparableDataSet resultDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.getResultFile())
                        .setSource(DataSourceType.fromString(this.getResultType()))
                        .setColumnSettings(this.expectDetailSettings)
                        .setEncoding(this.getOutputEncoding())
                        .build()
        );
    }

    public ComparableDataSet expectDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.getExpected())
                        .setSource(DataSourceType.fromString(this.getResultType()))
                        .setColumnSettings(this.expectDetailSettings)
                        .setEncoding(this.getOutputEncoding())
                        .build()
        );
    }

    public AddSettingColumns getExpectedComparisonKeys() {
        if (this.expectDetailSettings.getComparisonKeys().equals(AddSettingColumns.NONE)) {
            return AddSettingColumns.builder()
                    .addPattern(AddSettingColumns.ALL_MATCH_PATTERN, Lists.newArrayList())
                    .build();
        }
        return this.expectDetailSettings.getComparisonKeys();
    }

    public IDataSetWriter expectedDiffWriter() throws DataSetException {
        return this.writer(new File(this.getResultDir(), "expectedDiff"));
    }

    @Override
    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        super.populateSettings(parser);
        try {
            if (this.expectDetail != null) {
                this.expectDetailSettings = new FromJsonColumnSettingsBuilder().build(this.expectDetail);
            } else {
                this.expectDetailSettings = new FromJsonColumnSettingsBuilder().build();
            }
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        assertFileParameter(parser, this.newsource, this.newDir, "new");
        assertFileParameter(parser, this.oldsource, this.oldDir, "old");
    }

}
