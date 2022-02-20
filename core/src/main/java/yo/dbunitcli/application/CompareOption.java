package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.DataSetWriteOption;
import yo.dbunitcli.dataset.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CompareOption extends CommandLineOption {

    @Option(name = "-setting", usage = "file comparison settings")
    private File setting;

    @Option(name = "-expect", usage = "expected diff")
    private File expected;

    @Option(name = "-expectDetail", usage = "file define expected diff comparison settings")
    private File expectDetail;

    private DataSetLoadOption oldData = new DataSetLoadOption("old");

    private DataSetLoadOption newData = new DataSetLoadOption("new");

    private ColumnSettings expectDetailSettings;

    private ColumnSettings columnSettings;

    public CompareOption() {
        super(Parameter.none());
    }

    public CompareOption(Parameter param) {
        super(param);
    }

    public File getExpected() {
        return this.expected;
    }

    public DataSetLoadOption getOldData() {
        return oldData;
    }

    public DataSetLoadOption getNewData() {
        return newData;
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.newData.parseArgument(expandArgs);
        this.oldData.parseArgument(expandArgs);
        this.populateSettings(parser);
        this.getWriteOption().parseArgument(expandArgs);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-setting", this.setting);
        result.putAll(this.newData.expandOption(args));
        result.putAll(this.oldData.expandOption(args));
        result.putFile("-expect", this.expected);
        result.putFile("-expectDetail", this.expectDetail);
        result.putAll(this.getWriteOption().expandOption(args));
        return result;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.columnSettings.getComparisonKeys();
    }

    public ColumnSettings getColumnSettings() {
        return this.columnSettings;
    }

    public ComparableDataSet newDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.newData.getParam().build());
    }

    public ComparableDataSet oldDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.oldData.getParam().build());
    }

    public ComparableDataSet resultDataSet() throws DataSetException {
        DataSetWriteOption writeOption = this.getWriteOption();
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setColumnSettings(this.expectDetailSettings)
                        .setSrc(writeOption.getResultDir())
                        .setSource(writeOption.getResultType())
                        .setEncoding(writeOption.getOutputEncoding())
                        .build()
        );
    }

    public ComparableDataSet expectDataSet() throws DataSetException {
        DataSetWriteOption writeOption = this.getWriteOption();
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.getExpected())
                        .setColumnSettings(this.expectDetailSettings)
                        .setSource(writeOption.getResultType())
                        .setEncoding(writeOption.getOutputEncoding())
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
        return this.writer(new File(this.getWriteOption().getResultDir(), "expectedDiff"));
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
            if (this.expectDetail != null) {
                this.expectDetailSettings = new FromJsonColumnSettingsBuilder().build(this.expectDetail);
            } else {
                this.expectDetailSettings = new FromJsonColumnSettingsBuilder().build();
            }
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
