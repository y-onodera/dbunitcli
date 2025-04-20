package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ImageCompareOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.dataset.compare.CompareResult;
import yo.dbunitcli.dataset.compare.DataSetCompareBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CompareOption(
        Parameter parameter
        , ResultOption result
        , String setting
        , String settingEncoding
        , Type targetType
        , ImageCompareOption imageOption
        , DataSetLoadOption newData
        , DataSetLoadOption oldData
        , DataSetLoadOption expectData
) implements CommandLineOption<CompareDto> {

    private static final DefaultArgumentMapper IMAGE_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=png");
            return newArg.toArray(new String[0]);
        }
    };

    private static final DefaultArgumentMapper PDF_TYPE_PARAM_MAPPER = new DefaultArgumentMapper() {
        @Override
        public String[] map(final String[] arguments, final String prefix) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=pdf");
            return newArg.toArray(new String[0]);
        }
    };

    public static CompareDto toDto(final String[] args) {
        final CompareDto dto = new CompareDto();
        new CommandLineParser("", CommandLineOption.DEFAULT_COMMANDLINE_MAPPER, CommandLineOption.DEFAULT_COMMANDLINE_FILTER)
                .parseArgument(args, dto);
        if (dto.getTargetType().isAny(Type.image, Type.pdf)) {
            new CommandLineParser("image").parseArgument(args, dto.getImageOption());
        }
        if (dto.getTargetType() == Type.image) {
            new CommandLineParser("new", CompareOption.IMAGE_TYPE_PARAM_MAPPER)
                    .parseArgument(args, dto.getNewData());
            new CommandLineParser("old", CompareOption.IMAGE_TYPE_PARAM_MAPPER)
                    .parseArgument(args, dto.getOldData());
        } else if (dto.getTargetType() == Type.pdf) {
            new CommandLineParser("new", CompareOption.PDF_TYPE_PARAM_MAPPER)
                    .parseArgument(args, dto.getNewData());
            new CommandLineParser("old", CompareOption.PDF_TYPE_PARAM_MAPPER)
                    .parseArgument(args, dto.getOldData());
        } else {
            new CommandLineParser("new").parseArgument(args, dto.getNewData());
            new CommandLineParser("old").parseArgument(args, dto.getOldData());
        }
        if (Arrays.stream(args).anyMatch(it -> it.startsWith("-expect.srcType") || it.startsWith("-expect.src="))) {
            new CommandLineParser("expect").parseArgument(args, dto.getExpectData());
        } else {
            dto.getExpectData().setSrcType(DataSourceType.none);
        }
        new CommandLineParser("result").parseArgument(args, dto.getConvertResult());
        return dto;
    }

    private static Type getTargetType(final CompareDto dto) {
        return dto.getTargetType() != null ? dto.getTargetType() : Type.data;
    }

    public CompareOption(final String resultFile, final CompareDto dto, final Parameter param) {
        this(param
                , new ResultOption(resultFile, dto.getConvertResult())
                , dto.getSetting()
                , Strings.isNotEmpty(dto.getSettingEncoding())
                        ? dto.getSettingEncoding() : Charset.defaultCharset().displayName()
                , CompareOption.getTargetType(dto)
                , CompareOption.getTargetType(dto).isAny(Type.image, Type.pdf)
                        ? new ImageCompareOption("image", dto.getImageOption()) : new ImageCompareOption("image")
                , new DataSetLoadOption("new", dto.getNewData())
                , new DataSetLoadOption("old", dto.getOldData())
                , new DataSetLoadOption("expect", dto.getExpectData(), true));
    }

    @Override
    public CompareDto toDto() {
        return CompareOption.toDto(this.toArgs(true));
    }

    @Override
    public CommandLineArgsBuilder toCommandLineArgsBuilder() {
        final CommandLineArgsBuilder result = new CommandLineArgsBuilder()
                .put("-targetType", this.targetType, this.targetType.getDeclaringClass());
        final CommandLineArgsBuilder newDataCommandLineArgs = this.newData.toCommandLineArgsBuilder();
        final CommandLineArgsBuilder oldDataCommandLineArgs = this.oldData.toCommandLineArgsBuilder();
        if (this.targetType.isAny(Type.pdf, Type.image)) {
            result.addComponent("imageOption", this.imageOption.toCommandLineArgs());
            newDataCommandLineArgs.put("-srcType", DataSourceType.file, DataSourceType.class, Filter.include(DataSourceType.file), true);
            oldDataCommandLineArgs.put("-srcType", DataSourceType.file, DataSourceType.class, Filter.include(DataSourceType.file), true);
        }
        return result
                .putFile("-setting", this.setting, BaseDir.SETTING)
                .put("-settingEncoding", this.settingEncoding)
                .addComponent("newData", newDataCommandLineArgs.build())
                .addComponent("oldData", oldDataCommandLineArgs.build())
                .addComponent("convertResult", this.result().convertResult().toCommandLineArgsBuilder()
                        .put("-resultType", this.result().convertResult().resultType()
                                , ResultType.class, Filter.exclude(ResultType.table), true)
                        .build())
                .addComponent("expectData", this.expectData.toCommandLineArgsBuilder()
                        .put("-srcType", this.expectData.srcType(), DataSourceType.class
                                , Filter.include(DataSourceType.csv, DataSourceType.csvq, DataSourceType.xls, DataSourceType.xlsx, DataSourceType.sql, DataSourceType.none)
                                , false)
                        .build());
    }

    public boolean compare() {
        final CompareResult result = this.getDataSetCompareBuilder()
                .newDataSet(this.newDataSet())
                .oldDataSet(this.oldDataSet())
                .tableSeparators(this.getTableSeparators())
                .dataSetConverter(this.result().converter())
                .build()
                .result();
        if (this.expectData().srcType() != DataSourceType.none) {
            return !new DataSetCompareBuilder()
                    .newDataSet(this.resultDataSet())
                    .oldDataSet(this.expectDataSet())
                    .tableSeparators(this.expectData().getParam().getTableSeparators())
                    .dataSetConverter(this.expectedDiffConverter())
                    .build()
                    .result().existDiff();
        }
        return !result.existDiff();
    }

    public TableSeparators getTableSeparators() {
        try {
            if (this.targetType != Type.data) {
                return new FromJsonTableSeparatorsBuilder(this.settingEncoding)
                        .build().map(separator -> separator.addSetting(TableSeparator.builder()
                                .setComparisonKeys(List.of("NAME")).build()));
            } else {
                return new FromJsonTableSeparatorsBuilder(this.settingEncoding).build(this.setting);
            }
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
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
        final DataSetConverterOption converterOption = this.result().convertResult();
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setTableSeparators(this.getExpectTableSeparators())
                        .setSrc(converterOption.getResultDir())
                        .setSource(converterOption.resultType().toDataSourceType())
                        .setEncoding(converterOption.outputEncoding())
                        .setRecursive(false)
                        .build()
        );
    }

    public ComparableDataSet expectDataSet() {
        return this.getComparableDataSetLoader()
                .loadDataSet(this.expectData
                        .getParam()
                        .setRecursive(false)
                        .setTableSeparators(this.getExpectTableSeparators())
                        .build());
    }

    public DataSetCompareBuilder getDataSetCompareBuilder() {
        if (this.targetType == Type.data) {
            return new DataSetCompareBuilder();
        }
        return new DataSetCompareBuilder()
                .setCompareManagerFactory(this.imageOption.createFactoryOf(this.targetType));
    }

    public IDataSetConverter expectedDiffConverter() {
        return this.result().converter(it ->
                it.setResultDir(new File(this.result().convertResult().resultDir(), "expectedDiff")));
    }

    private TableSeparators getExpectTableSeparators() {
        if (Strings.isEmpty(this.expectData.getSetting())) {
            return TableSeparators.NONE;
        }
        return this.expectData.getParam().getTableSeparators();
    }

    public enum Type {
        data, image, pdf;

        public boolean isAny(final Type... expects) {
            return Stream.of(expects).anyMatch(it -> it == this);
        }
    }
}
