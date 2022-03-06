package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.DataSetWriteOption;
import yo.dbunitcli.dataset.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class CompareOption extends CommandLineOption {

    @Option(name = "-setting", usage = "file comparison settings")
    private File setting;

    private final DataSetLoadOption expectData = new DataSetLoadOption("expect");

    private final DataSetLoadOption oldData = new DataSetLoadOption("old");

    private final DataSetLoadOption newData = new DataSetLoadOption("new");

    private ColumnSettings columnSettings;

    public CompareOption() {
        super(Parameter.none());
    }

    public CompareOption(Parameter param) {
        super(param);
    }

    public DataSetLoadOption getOldData() {
        return this.oldData;
    }

    public DataSetLoadOption getNewData() {
        return this.newData;
    }

    public DataSetLoadOption getExpectData() {
        return this.expectData;
    }

    @Override
    public void setUpComponent(CmdLineParser parser, String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        this.newData.parseArgument(expandArgs);
        this.oldData.parseArgument(expandArgs);
        if (Arrays.stream(expandArgs).anyMatch(it -> it.startsWith("-expect.src"))) {
            this.expectData.parseArgument(expandArgs);
            if (Arrays.stream(expandArgs).noneMatch(it -> it.startsWith("-expect.setting"))) {
                this.expectData.getParam().editColumnSettings(editor -> editor.setKeyEdit(it ->
                        it.addPattern(AddSettingColumns.ALL_MATCH_PATTERN, Lists.newArrayList())
                ));
            }
        }
        this.populateSettings(parser);
        this.getWriteOption().parseArgument(expandArgs);
    }

    @Override
    public OptionParam expandOption(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-setting", this.setting);
        result.putAll(this.newData.expandOption(args));
        result.putAll(this.oldData.expandOption(args));
        result.putAll(this.getWriteOption().expandOption(args));
        result.putAll(this.expectData.expandOption(args));
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
                        .setColumnSettings(this.expectData.getParam().getColumnSettings())
                        .setSrc(writeOption.getResultDir())
                        .setSource(writeOption.getResultType())
                        .setEncoding(writeOption.getOutputEncoding())
                        .build()
        );
    }

    public ComparableDataSet expectDataSet() throws DataSetException {
        return this.getComparableDataSetLoader().loadDataSet(this.expectData.getParam().build());
    }

    public IDataSetWriter expectedDiffWriter() throws DataSetException {
        return this.writer(new File(this.getWriteOption().getResultDir(), "expectedDiff"));
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
    }
}
