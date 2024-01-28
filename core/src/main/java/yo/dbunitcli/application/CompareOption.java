package yo.dbunitcli.application;

import picocli.CommandLine;
import yo.dbunitcli.application.argument.DataSetConverterOption;
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
import java.util.stream.Stream;

public class CompareOption extends CommandLineOption {

    public static final DefaultArgumentMapper IMAGE_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix, final CommandLine cmdLine) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=png");
            return newArg.toArray(new String[0]);
        }
    };

    public static final DefaultArgumentMapper PDF_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix, final CommandLine cmdLine) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=pdf");
            return newArg.toArray(new String[0]);
        }
    };

    private final DataSetLoadOption expectData = new DataSetLoadOption("expect");
    private final DataSetLoadOption oldData = new DataSetLoadOption("old");
    private final DataSetLoadOption newData = new DataSetLoadOption("new");
    private final ImageCompareOption imageOption = new ImageCompareOption("image");
    @CommandLine.Option(names = "-setting", description = "file comparison settings")
    private String setting;
    @CommandLine.Option(names = "-settingEncoding", description = "settings encoding")
    private String settingEncoding = System.getProperty("file.encoding");
    @CommandLine.Option(names = "-targetType")
    private Type targetType = Type.data;
    private TableSeparators tableSeparators;

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
    public void setUpComponent(final CommandLine.ParseResult parseResult, final String[] expandArgs) {
        super.setUpComponent(parseResult, expandArgs);
        if (this.targetType.isAny(Type.image, Type.pdf)) {
            this.imageOption.parseArgument(expandArgs);
            if (this.targetType == Type.image) {
                this.newData.setArgumentMapper(CompareOption.IMAGE_TYPE_PARAM_MAPPER);
                this.oldData.setArgumentMapper(CompareOption.IMAGE_TYPE_PARAM_MAPPER);
            } else if (this.targetType == Type.pdf) {
                this.newData.setArgumentMapper(CompareOption.PDF_TYPE_PARAM_MAPPER);
                this.oldData.setArgumentMapper(CompareOption.PDF_TYPE_PARAM_MAPPER);
            }
        }
        this.newData.parseArgument(expandArgs);
        this.oldData.parseArgument(expandArgs);
        if (Arrays.stream(expandArgs).anyMatch(it -> it.startsWith("-expect.src"))) {
            this.expectData.parseArgument(expandArgs);
            if (Arrays.stream(expandArgs).noneMatch(it -> it.startsWith("-expect.setting"))) {
                this.expectData.getParam().editColumnSettings(separator ->
                        separator.addSetting(TableSeparator.builder().build())
                );
            }
        }
        this.populateSettings();
        this.getConverterOption().parseArgument(expandArgs);
    }

    @Override
    public OptionParam createOptionParam(final Map<String, String> args) {
        final OptionParam result = new OptionParam(this.getPrefix(), args);
        result.put("-targetType", this.targetType, Type.class);
        if (Type.valueOf(result.get("-targetType")).isAny(Type.pdf, Type.image)) {
            result.putAll(this.imageOption.createOptionParam(args));
        }
        result.putFile("-setting", this.setting == null ? null : new File(this.setting));
        result.putAll(this.newData.createOptionParam(args));
        result.putAll(this.oldData.createOptionParam(args));
        result.putAll(this.getConverterOption().createOptionParam(args));
        result.putAll(this.expectData.createOptionParam(args));
        return result;
    }

    public boolean compare() {
        final CompareResult result = this.getDataSetCompareBuilder()
                .newDataSet(this.newDataSet())
                .oldDataSet(this.oldDataSet())
                .tableSeparators(this.getTableSeparators())
                .dataSetConverter(this.converter())
                .build()
                .result();
        if (this.getExpectData().getParam().getSrc() != null) {
            return !new DataSetCompareBuilder()
                    .newDataSet(this.resultDataSet())
                    .oldDataSet(this.expectDataSet())
                    .tableSeparators(this.getExpectData().getParam().getTableSeparators())
                    .dataSetConverter(this.expectedDiffConverter())
                    .build()
                    .result().existDiff();
        }
        return !result.existDiff();
    }

    public TableSeparators getTableSeparators() {
        return this.tableSeparators;
    }

    public ComparableDataSet newDataSet() {
        final ComparableDataSetParam.Builder loadParam = this.newData.getParam()
                .ifMatch(this.targetType != Type.data
                        , it -> it.editColumnSettings(separator ->
                                separator.setCommonRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder()
                                                .setNewName("TARGET").build())
                                        .addSetting(TableSeparator.builder().setComparisonKeys(List.of("NAME"))
                                                .build())
                        )
                );
        return this.getComparableDataSetLoader().loadDataSet(loadParam.build());
    }

    public ComparableDataSet oldDataSet() {
        final ComparableDataSetParam.Builder loadParam = this.oldData.getParam()
                .ifMatch(this.targetType != Type.data
                        , it -> it.editColumnSettings(separator ->
                                separator.setCommonRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder()
                                                .setNewName("TARGET").build())
                                        .addSetting(TableSeparator.builder().setComparisonKeys(List.of("NAME"))
                                                .build())
                        )
                );
        return this.getComparableDataSetLoader().loadDataSet(loadParam.build());
    }

    public ComparableDataSet resultDataSet() {
        final DataSetConverterOption converterOption = this.getConverterOption();
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setTableSeparators(this.expectData.getParam().getTableSeparators())
                        .setSrc(converterOption.getResultDir())
                        .setSource(converterOption.getResultType())
                        .setEncoding(converterOption.getOutputEncoding())
                        .setRecursive(false)
                        .build()
        );
    }

    public ComparableDataSet expectDataSet() {
        return this.getComparableDataSetLoader().loadDataSet(this.expectData.getParam().setRecursive(false).build());
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

    protected void populateSettings() {
        try {
            this.tableSeparators = new FromJsonTableSeparatorsBuilder(this.settingEncoding).build(this.setting);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
        if (this.targetType != Type.data) {
            this.tableSeparators = this.tableSeparators.map(separator -> separator.addSetting(TableSeparator.builder()
                    .setComparisonKeys(List.of("NAME")).build())
            );
        }
    }

    public enum Type {
        data, image, pdf;

        public boolean isAny(final Type... expects) {
            return Stream.of(expects).anyMatch(it -> it == this);
        }
    }
}
