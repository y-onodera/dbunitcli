package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.DefaultArgumentFunction;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.common.Parameter;
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

    private static final DefaultArgumentFunction IMAGE_TYPE_PARAM_MAPPER = new DefaultArgumentFunction() {
        @Override
        public String[] apply(final String[] arguments, final String prefix) {
            final List<String> newArg = Arrays.stream(arguments)
                    .filter(it -> !it.contains("srcType=") && !it.contains("extension="))
                    .collect(Collectors.toList());
            newArg.add("-srcType=file");
            newArg.add("-extension=png");
            return newArg.toArray(new String[0]);
        }
    };

    private static final DefaultArgumentFunction PDF_TYPE_PARAM_MAPPER = new DefaultArgumentFunction() {
        @Override
        public String[] apply(final String[] arguments, final String prefix) {
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
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        if (dto.getTargetType().isAny(Type.image, Type.pdf)) {
            new ArgumentMapper("image").populate(args, dto.getImageOption());
        }
        if (dto.getTargetType() == Type.image) {
            new ArgumentMapper("new", CompareOption.IMAGE_TYPE_PARAM_MAPPER)
                    .populate(args, dto.getNewData());
            new ArgumentMapper("old", CompareOption.IMAGE_TYPE_PARAM_MAPPER)
                    .populate(args, dto.getOldData());
        } else if (dto.getTargetType() == Type.pdf) {
            new ArgumentMapper("new", CompareOption.PDF_TYPE_PARAM_MAPPER)
                    .populate(args, dto.getNewData());
            new ArgumentMapper("old", CompareOption.PDF_TYPE_PARAM_MAPPER)
                    .populate(args, dto.getOldData());
        } else {
            new ArgumentMapper("new").populate(args, dto.getNewData());
            new ArgumentMapper("old").populate(args, dto.getOldData());
        }
        if (Arrays.stream(args).anyMatch(it -> it.startsWith("-expect.srcType") || it.startsWith("-expect.src="))) {
            new ArgumentMapper("expect").populate(args, dto.getExpectData());
        } else {
            dto.getExpectData().setSrcType(DataSourceType.none);
        }
        new ArgumentMapper("result").populate(args, dto.getConvertResult());
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
    public ParametersBuilder toParametersBuilder() {
        final ParametersBuilder result = new ParametersBuilder()
                .put("-targetType", this.targetType, this.targetType.getDeclaringClass());
        final ParametersBuilder newDataCommandLineArgs = this.newData.toParametersBuilder();
        final ParametersBuilder oldDataCommandLineArgs = this.oldData.toParametersBuilder();
        if (this.targetType.isAny(Type.pdf, Type.image)) {
            result.addComponent("imageOption", this.imageOption.toParameters());
            newDataCommandLineArgs.put("-srcType", DataSourceType.file, DataSourceType.class, Filter.include(DataSourceType.file), true);
            oldDataCommandLineArgs.put("-srcType", DataSourceType.file, DataSourceType.class, Filter.include(DataSourceType.file), true);
        }
        return result
                .putFile("-setting", this.setting, BaseDir.SETTING)
                .put("-settingEncoding", this.settingEncoding)
                .addComponent("newData", newDataCommandLineArgs.build())
                .addComponent("oldData", oldDataCommandLineArgs.build())
                .addComponent("convertResult", this.result().convertResult().toParametersBuilder()
                        .put("-resultType", this.result().convertResult().resultType()
                                , ResultType.class, Filter.exclude(ResultType.table), true)
                        .build())
                .addComponent("expectData", this.expectData.toParametersBuilder()
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
