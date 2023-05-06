package yo.dbunitcli.application;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.application.argument.DataSetConverterOption;
import yo.dbunitcli.application.argument.DataSetLoadOption;
import yo.dbunitcli.application.argument.DefaultArgumentMapper;
import yo.dbunitcli.application.argument.ImageCompareOption;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.dataset.compare.CompareResult;
import yo.dbunitcli.dataset.compare.DataSetCompareBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompareOption extends CommandLineOption {

    public static final DefaultArgumentMapper IMAGE_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix, final CmdLineParser parser) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=png");
            return newArg.toArray(new String[]{});
        }
    };

    public static final DefaultArgumentMapper PDF_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix, final CmdLineParser parser) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=pdf");
            return newArg.toArray(new String[]{});
        }
    };

    @Option(name = "-setting", usage = "file comparison settings")
    private String setting;

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

    public CompareOption(final Parameter param) {
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
    public void setUpComponent(final CmdLineParser parser, final String[] expandArgs) throws CmdLineException {
        super.setUpComponent(parser, expandArgs);
        if (this.targetType == Type.image) {
            this.newData.setArgumentMapper(IMAGE_TYPE_PARAM_MAPPER);
            this.oldData.setArgumentMapper(IMAGE_TYPE_PARAM_MAPPER);
        } else if (this.targetType == Type.pdf) {
            this.newData.setArgumentMapper(PDF_TYPE_PARAM_MAPPER);
            this.oldData.setArgumentMapper(PDF_TYPE_PARAM_MAPPER);
        }

        this.newData.parseArgument(expandArgs);
        this.oldData.parseArgument(expandArgs);
        if (Arrays.stream(expandArgs).anyMatch(it -> it.startsWith("-expect.src"))) {
            this.expectData.parseArgument(expandArgs);
            if (Arrays.stream(expandArgs).noneMatch(it -> it.startsWith("-expect.setting"))) {
                this.expectData.getParam().editColumnSettings(editor -> editor.setKeyEdit(it ->
                        it.addPattern(AddSettingColumns.ALL_MATCH_PATTERN, new ArrayList<>())
                ));
            }
        }
        this.populateSettings(parser);
        this.getConverterOption().parseArgument(expandArgs);
        if (this.targetType == Type.image || this.targetType == Type.pdf) {
            this.imageOption.parseArgument(expandArgs);
        }
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.putFile("-setting", this.setting == null ? null : new File(this.setting));
        result.putAll(this.newData.createOptionParam(args));
        result.putAll(this.oldData.createOptionParam(args));
        result.putAll(this.getConverterOption().createOptionParam(args));
        result.putAll(this.expectData.createOptionParam(args));
        result.putAll(this.imageOption.createOptionParam(args));
        return result;
    }

    public CompareResult compare() {
        final CompareResult result = this.getDataSetCompareBuilder()
                .newDataSet(this.newDataSet())
                .oldDataSet(this.oldDataSet())
                .comparisonKeys(this.getComparisonKeys())
                .dataSetConverter(this.converter())
                .build()
                .result();
        if (this.getExpectData().getParam().getSrc() != null) {
            if (new DataSetCompareBuilder()
                    .newDataSet(this.resultDataSet())
                    .oldDataSet(this.expectDataSet())
                    .comparisonKeys(this.getExpectData().getParam().getColumnSettings().getComparisonKeys())
                    .dataSetConverter(this.expectedDiffConverter())
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

    public ComparableDataSet newDataSet() {
        final ComparableDataSetParam.Builder loadParam = this.newData.getParam()
                .ifMatch(this.targetType != Type.data
                        , it -> it.setColumnSettings(it
                                .getColumnSettings()
                                .apply(builder -> builder.setTableRenameFunctionEdit(origin -> (String name) -> "TARGET")))
                );
        return this.getComparableDataSetLoader().loadDataSet(loadParam.build());
    }

    public ComparableDataSet oldDataSet() {
        final ComparableDataSetParam.Builder loadParam = this.oldData.getParam()
                .ifMatch(this.targetType != Type.data
                        , it -> it.setColumnSettings(it
                                .getColumnSettings()
                                .apply(builder -> builder.setTableRenameFunctionEdit(origin -> (String name) -> "TARGET")))
                );
        return this.getComparableDataSetLoader().loadDataSet(loadParam.build());
    }

    public ComparableDataSet resultDataSet() {
        final DataSetConverterOption converterOption = this.getConverterOption();
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setColumnSettings(this.expectData.getParam().getColumnSettings())
                        .setSrc(converterOption.getResultDir())
                        .setSource(converterOption.getResultType())
                        .setEncoding(converterOption.getOutputEncoding())
                        .build()
        );
    }

    public ComparableDataSet expectDataSet() {
        return this.getComparableDataSetLoader().loadDataSet(this.expectData.getParam().build());
    }

    public DataSetCompareBuilder getDataSetCompareBuilder() {
        if (this.targetType == Type.data) {
            return new DataSetCompareBuilder();
        }
        return new DataSetCompareBuilder().setCompareManagerFactory(this.imageOption.createFactoryOf(this.targetType));
    }

    public IDataSetConverter expectedDiffConverter() {
        this.getConverterOption().setResultDir(new File(this.getConverterOption().getResultDir(), "expectedDiff"));
        return this.converter();
    }

    protected void populateSettings(final CmdLineParser parser) throws CmdLineException {
        try {
            this.columnSettings = new FromJsonColumnSettingsBuilder().build(this.setting);
        } catch (final IOException e) {
            throw new CmdLineException(parser, e);
        }
        if (this.targetType != Type.data) {
            this.columnSettings = this.columnSettings.apply(it -> it
                    .setKeyEdit(setting -> setting.addPattern(AddSettingColumns.ALL_MATCH_PATTERN, List.of("NAME")))
            );
        }
    }

    public enum Type {
        data, image, pdf
    }
}
