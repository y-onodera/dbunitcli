package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetConsumerOption;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.DefaultArgumentMapper;
import yo.dbunitcli.application.argument.ImageCompareOption;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.dataset.compare.CompareResult;
import yo.dbunitcli.dataset.compare.DataSetCompareBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompareOption extends CommandLineOption {

    public static final DefaultArgumentMapper IMAGE_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(String[] arguments, String prefix, CmdLineParser parser) {
            List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=png");
            return newArg.toArray(new String[]{});
        }
    };

    @Option(name = "-setting", usage = "file comparison settings")
    private File setting;

    @Option(name = "-targetType")
    private Type targetType = Type.data;

    private final DataSetLoadOption expectData = new DataSetLoadOption("expect");

    private final DataSetLoadOption oldData = new DataSetLoadOption("old");

    private final DataSetLoadOption newData = new DataSetLoadOption("new");

    private final ImageCompareOption imageOption = new ImageCompareOption("image");

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
        if (this.targetType == Type.image) {
            this.newData.setArgumentMapper(IMAGE_TYPE_PARAM_MAPPER);
            this.oldData.setArgumentMapper(IMAGE_TYPE_PARAM_MAPPER);
        }
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
        this.getConsumerOption().parseArgument(expandArgs);
        if (this.targetType == Type.image) {
            this.imageOption.parseArgument(expandArgs);
        }
    }

    @Override
    public OptionParam createOptionParam(Map<String, String> args) {
        OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-setting", this.setting);
        result.putAll(this.newData.createOptionParam(args));
        result.putAll(this.oldData.createOptionParam(args));
        result.putAll(this.getConsumerOption().createOptionParam(args));
        result.putAll(this.expectData.createOptionParam(args));
        result.putAll(this.imageOption.createOptionParam(args));
        return result;
    }

    public CompareResult compare() throws DataSetException {
        ComparableDataSet oldData = this.oldDataSet();
        ComparableDataSet newData = this.newDataSet();
        IDataSetConsumer writer = this.consumer();
        CompareResult result = this.getDataSetCompareBuilder()
                .newDataSet(newData)
                .oldDataSet(oldData)
                .comparisonKeys(this.getComparisonKeys())
                .dataSetWriter(writer)
                .build()
                .result();
        if (this.getExpectData().getParam().getSrc() != null) {
            if (new DataSetCompareBuilder()
                    .newDataSet(this.resultDataSet())
                    .oldDataSet(this.expectDataSet())
                    .comparisonKeys(this.getExpectData().getParam().getColumnSettings().getComparisonKeys())
                    .dataSetWriter(this.expectedDiffWriter())
                    .build()
                    .result().existDiff()) {
                throw new AssertionError("unexpected diff found.");
            }
        } else {
            if (result.existDiff()) {
                throw new AssertionError("unexpected diff found.");
            }
        }
        return result;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.columnSettings.getComparisonKeys();
    }

    public ColumnSettings getColumnSettings() {
        return this.columnSettings;
    }

    public ComparableDataSet newDataSet() throws DataSetException {
        ComparableDataSetParam.Builder loadParam = newData.getParam()
                .ifMatch(this.targetType != Type.data
                        , it -> it.setColumnSettings(it
                                .getColumnSettings()
                                .apply(builder -> builder.setTableNameMapEdit(origin -> (String name) -> "TARGET")))
                );
        return this.getComparableDataSetLoader().loadDataSet(loadParam.build());
    }

    public ComparableDataSet oldDataSet() throws DataSetException {
        ComparableDataSetParam.Builder loadParam = oldData.getParam()
                .ifMatch(this.targetType != Type.data
                        , it -> it.setColumnSettings(it
                                .getColumnSettings()
                                .apply(builder -> builder.setTableNameMapEdit(origin -> (String name) -> "TARGET")))
                );
        return this.getComparableDataSetLoader().loadDataSet(loadParam.build());
    }

    public ComparableDataSet resultDataSet() throws DataSetException {
        DataSetConsumerOption writeOption = this.getConsumerOption();
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

    public DataSetCompareBuilder getDataSetCompareBuilder() {
        if (this.targetType == Type.data) {
            return new DataSetCompareBuilder();
        }
        return this.imageOption.getDataSetCompareBuilder();
    }

    public IDataSetConsumer expectedDiffWriter() throws DataSetException {
        this.getConsumerOption().setResultDir(new File(this.getConsumerOption().getResultDir(), "expectedDiff"));
        return this.consumer();
    }

    protected void populateSettings(CmdLineParser parser) throws CmdLineException {
        try {
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
        } catch (IOException e) {
            throw new CmdLineException(parser, e);
        }
        if (this.targetType != Type.data) {
            this.columnSettings = this.columnSettings.apply(it -> it
                    .setKeyEdit(setting -> setting.addPattern(AddSettingColumns.ALL_MATCH_PATTERN, Lists.newArrayList("NAME")))
            );
        }
    }

    public enum Type {
        data, image, pdf
    }
}
