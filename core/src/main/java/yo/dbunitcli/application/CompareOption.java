package yo.dbunitcli.application;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.cli.CommandLineParser;
import yo.dbunitcli.application.cli.DefaultArgumentMapper;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.ImageCompareOption;
import yo.dbunitcli.dataset.*;
import yo.dbunitcli.dataset.compare.CompareResult;
import yo.dbunitcli.dataset.compare.DataSetCompareBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompareOption extends CommandLineOption<CompareDto> {

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
    private final DataSetLoadOption oldData;
    private final DataSetLoadOption newData;
    private final String setting;
    private final String settingEncoding;
    private final Type targetType;
    private final DataSetLoadOption expectData;
    private final ImageCompareOption imageOption;

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

    public CompareOption(final String resultFile, final CompareDto dto, final Parameter param) {
        super(resultFile, dto, param);
        this.setting = dto.getSetting();
        if (Strings.isNotEmpty(dto.getSettingEncoding())) {
            this.settingEncoding = dto.getSettingEncoding();
        } else {
            this.settingEncoding = System.getProperty("file.encoding");
        }
        if (dto.getTargetType() != null) {
            this.targetType = dto.getTargetType();
        } else {
            this.targetType = Type.data;
        }
        if (this.targetType.isAny(Type.image, Type.pdf)) {
            this.imageOption = new ImageCompareOption("image", dto.getImageOption());
        } else {
            this.imageOption = new ImageCompareOption("image");
        }
        this.newData = new DataSetLoadOption("new", dto.getNewData());
        this.oldData = new DataSetLoadOption("old", dto.getOldData());
        this.expectData = new DataSetLoadOption("expect", dto.getExpectData());
    }

    @Override
    public CompareDto toDto() {
        return CompareOption.toDto(this.toArgs(true));
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
    public CommandLineArgs toCommandLineArgs() {
        final CommandLineArgs result = new CommandLineArgs();
        result.put("-targetType", this.targetType, Type.class);
        if (Type.valueOf(result.get("-targetType")).isAny(Type.pdf, Type.image)) {
            result.addComponent("imageOption", this.imageOption.toCommandLineArgs());
        }
        result.putFile("-setting", this.setting == null ? null : new File(this.setting));
        result.put("-settingEncoding", this.settingEncoding);
        result.addComponent("newData", this.newData.toCommandLineArgs());
        result.addComponent("oldData", this.oldData.toCommandLineArgs());
        result.addComponent("convertResult", this.getConvertResult().toCommandLineArgs());
        result.addComponent("expectData", this.expectData.toCommandLineArgs());
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
        if (this.getExpectData().srcType() != DataSourceType.none) {
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
        final DataSetConverterOption converterOption = this.getConvertResult();
        return this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setTableSeparators(this.getExpectTableSeparators())
                        .setSrc(converterOption.getResultDir())
                        .setSource(converterOption.getResultType().toDataSourceType())
                        .setEncoding(converterOption.getOutputEncoding())
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
        return this.converter(it ->
                it.setResultDir(new File(this.getConvertResult().getResultDir(), "expectedDiff")));
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
