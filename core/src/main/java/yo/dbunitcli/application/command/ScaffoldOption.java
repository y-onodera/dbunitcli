package yo.dbunitcli.application.command;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.ITable;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.ResultType;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;

public record ScaffoldOption(
        Parameter parameter
        , String resultDir
        , String target
        , String unitSettingName
        , String templateName
        , String parameterName
        , String commandType
        , String[] commandInput
        , DataSetLoadOption dataset
        , ResultType datasetType
        , String datasetEncoding
) implements CommandLineOption<ScaffoldDto> {

    private static final String COMMAND_INPUT_PREFIX = "-commandInput.";
    private static final String DATASET_SRC_DIR = "src";

    public ScaffoldOption {
        if (datasetType == ResultType.format) {
            throw new AssertionError("-datasetType=format is not supported. use csv, xls, xlsx, fixed or table",
                                     new IllegalArgumentException(String.valueOf(datasetType)));
        }
    }

    public ScaffoldOption(final String resultFile, final ScaffoldDto dto, final Parameter param) {
        this(param
                , Strings.isNotEmpty(dto.getResultDir()) ? dto.getResultDir() : resultFile
                , Strings.isNotEmpty(dto.getTarget()) ? dto.getTarget() : ""
                , Strings.isNotEmpty(dto.getUnitSettingName()) ? dto.getUnitSettingName() : ""
                , Strings.isNotEmpty(dto.getTemplateName()) ? dto.getTemplateName() : ""
                , Strings.isNotEmpty(dto.getParameterName()) ? dto.getParameterName() : ""
                , Strings.isNotEmpty(dto.getCommandType()) ? dto.getCommandType() : ""
                , dto.getCommandInput()
                , new DataSetLoadOption("dataset", dto.getDatasetDto(), true)
                , dto.getDatasetType() != null ? dto.getDatasetType() : ResultType.csv
                , Strings.isNotEmpty(dto.getDatasetEncoding()) ? dto.getDatasetEncoding() : "UTF-8"
        );
    }

    public static ScaffoldDto toDto(final String[] args) {
        final ScaffoldDto dto = new ScaffoldDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER)
                .populate(args, dto);
        dto.setCommandInput(Arrays.stream(args)
                                  .filter(it -> it.startsWith(COMMAND_INPUT_PREFIX))
                                  .map(it -> "-" + it.substring(COMMAND_INPUT_PREFIX.length()))
                                  .toArray(String[]::new));
        final String[] datasetArgs = Arrays.stream(args)
                                              .filter(it -> it.startsWith("-dataset."))
                                              .toArray(String[]::new);
        new ArgumentMapper("dataset").populate(datasetArgs, dto.getDatasetDto());
        return dto;
    }

    public void execute() throws IOException {
        final File baseDir = FileResources.resultDir(this.resultDir);
        final ScaffoldTarget scaffoldTarget = ScaffoldTarget.fromString(this.target);
        if (scaffoldTarget != null) {
            this.executeTarget(scaffoldTarget, baseDir);
        }
        if ("parameter".equals(this.target) && Strings.isNotEmpty(this.commandType)) {
            final File paramDir = new File(baseDir, "option");
            if (paramDir.mkdirs() || paramDir.isDirectory()) {
                final String[] shrunkArgs = new CommandParameters(Type.valueOf(this.commandType), this.commandInput)
                        .shrink().args();
                Files.write(new File(paramDir, this.commandType + ".param").toPath(),
                            Arrays.asList(shrunkArgs), StandardCharsets.UTF_8);
            }
        }
    }

    // 4target共通フロー: unitSetting/template/dataset/parameterの各雛型を、対応オプションが
    // 明示指定された時のみ独立に出力する（dataset雛型のみ-dataset.src/-dataset.srcTypeの指定が条件）
    private void executeTarget(final ScaffoldTarget scaffoldTarget, final File baseDir) throws IOException {
        if (Strings.isNotEmpty(this.unitSettingName)) {
            final File settingDir = new File(baseDir, FileResources.RESOURCES_SETTING_PATH);
            if (settingDir.mkdirs() || settingDir.isDirectory()) {
                this.copyClasspathResource(scaffoldTarget.sampleUnitSettingPath(),
                                           new File(settingDir, this.unitSettingName + ".json"));
            }
        }
        if (Strings.isNotEmpty(this.templateName)) {
            final File templateDir = new File(baseDir, FileResources.RESOURCES_TEMPLATE_PATH);
            if (templateDir.mkdirs() || templateDir.isDirectory()) {
                this.copyClasspathResource(scaffoldTarget.stgPath(), new File(templateDir, this.templateName + ".stg"));
                this.copyClasspathResource(scaffoldTarget.templatePath(), new File(templateDir, this.templateName + ".txt"));
            }
        }
        if (this.hasDataset()) {
            this.writeWrappedDatasetSrcFiles(new File(baseDir, DATASET_SRC_DIR),
                                             scaffoldTarget.wrapProducer(this, this.sourceProducer()));
        }
        if (Strings.isNotEmpty(this.parameterName)) {
            final File paramDir = new File(baseDir, "option");
            if (paramDir.mkdirs() || paramDir.isDirectory()) {
                this.writeParamFile(paramDir, scaffoldTarget);
            }
        }
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir);
    }

    @Override
    public ScaffoldDto toDto() {
        return ScaffoldOption.toDto(this.toArgs(true));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        final ParametersBuilder result = new ParametersBuilder();
        result.putDir("-result", this.resultDir, BaseDir.RESULT)
              .put("-target", this.target);
        result.put("-unitSetting", this.unitSettingName)
              .put("-template", this.templateName)
              .put("-parameter", this.parameterName);
        result.put("-commandType", this.commandType);
        Arrays.stream(this.commandInput)
              .filter(arg -> arg.startsWith("-"))
              .forEach(arg -> {
                  final int eqIdx = arg.indexOf('=');
                  final String key = COMMAND_INPUT_PREFIX + arg.substring(1, eqIdx > 0 ? eqIdx : arg.length());
                  result.put(key, eqIdx > 0 ? arg.substring(eqIdx + 1) : "true");
              });
        if (this.hasDataset()) {
            result.put("-datasetType", this.datasetType, ResultType.class);
            result.put("-datasetEncoding", this.datasetEncoding);
            result.addComponent("dataset", this.dataset.toParametersBuilder().build());
        }
        return result;
    }

    private boolean hasDataset() {
        return this.dataset != null
                && this.dataset.srcType() != null
                && this.dataset.srcType() != DataSourceType.none
                && Strings.isNotEmpty(this.dataset.src());
    }

    private boolean isHeaderlessDataset() {
        return this.datasetType == ResultType.fixed;
    }

    private boolean usesDatasetEncoding() {
        return this.datasetType == ResultType.csv || this.datasetType == ResultType.fixed;
    }

    private void writeWrappedDatasetSrcFiles(final File srcDir,
                                              final ComparableDataSetProducerWrapper wrapped) throws IOException {
        this.writeConverted(srcDir, Arrays.asList(wrapped.loadDataSet().getTables()));
    }

    private void writeConverted(final File srcDir, final Iterable<? extends ITable> tables) throws IOException {
        if (!srcDir.mkdirs() && !srcDir.isDirectory()) {
            return;
        }
        final IDataSetConverter converter = this.createSrcConverter(srcDir);
        converter.startDataSet();
        for (final ITable table : tables) {
            converter.convert(table);
        }
        converter.endDataSet();
    }

    private ComparableDataSetProducer sourceProducer() {
        return new ComparableDataSetLoader(this.parameter).getComparableDataSetProducer(this.sourceParam());
    }

    private ComparableDataSetParam sourceParam() {
        return this.dataset.getParam()
                .setLoadData(false)
                .build();
    }

    private IDataSetConverter createSrcConverter(final File srcDir) {
        final DataSetConverterParam converterParam = DataSetConverterParam.builder()
                .setResultType(this.datasetType)
                .setResultDir(srcDir)
                .setExportEmptyTable(true)
                .setSkipHeader(!this.isHeaderlessDataset())
                .setOutputEncoding(this.datasetEncoding)
                .build();
        return new DataSetConverterLoader().get(converterParam);
    }

    private void writeParamFile(final File paramDir, final ScaffoldTarget scaffoldTarget) throws IOException {
        final ParametersBuilder builder = new ParametersBuilder();
        final boolean customTemplate = Strings.isNotEmpty(this.templateName);
        if (customTemplate) {
            // カスタムテンプレートはgenerateType=txt＋unitSettingで駆動し、組み込みテンプレートが
            // 読むのと同じrows/tableName/dataset.PK属性を再現する（入力はscaffoldが書き出した記述子dataset）
            this.putTemplateGenerationParams(builder, this.templateName);
            builder.put("-resultPath", scaffoldTarget.customTemplateResultPath());
        } else {
            // 組み込みgenerateTypeはloadData()=falseで元ソースのメタデータから記述子行を合成するため、
            // -settingを書くと変換ルールが合成前の元ソース列に対して評価されてしまう（unitSettingのみ有効）
            builder.put("-generateType", scaffoldTarget.generateType().name(), false);
        }
        if (Strings.isNotEmpty(this.unitSettingName)) {
            builder.put("-unitSetting", "resources/setting/" + this.unitSettingName + ".json");
        }
        builder.putDir("-result", this.resultDir, BaseDir.RESULT);
        if (this.hasDataset()) {
            if (customTemplate) {
                this.addDatasetSrcParams(builder);
                if (this.isHeaderlessDataset()) {
                    builder.put("-headerName", ScaffoldOption.headerNames(scaffoldTarget.datasetSchema()));
                }
            } else {
                this.addOriginalDatasetSrcParams(builder);
            }
        }
        Files.write(new File(paramDir, this.parameterName + ".param").toPath(),
                    builder.build().toList(false), StandardCharsets.UTF_8);
    }

    // 組み込みgenerateTypeで駆動する.paramは、scaffoldが書き出した記述子datasetではなく
    // 元の-dataset.*をsrc.*として再現する（組み込み型は元ソースから記述子行を合成するため）。
    // .paramは別workspaceでも実行されるため-src.srcは絶対パスへ解決して書き出す
    private void addOriginalDatasetSrcParams(final ParametersBuilder builder) {
        this.dataset.toParametersBuilder().build().toList(false).forEach(line -> {
            final int eqIdx = line.indexOf('=');
            builder.put(line.substring(0, eqIdx).replaceFirst("^-dataset\\.", "-src."), line.substring(eqIdx + 1));
        });
        builder.put("-src.src",
                    FileResources.searchDatasetBase(this.dataset.src()).getAbsolutePath().replace('\\', '/'));
    }

    private void putTemplateGenerationParams(final ParametersBuilder builder, final String templateFileName) {
        builder.put("-generateType", GenerateType.txt.name(), false);
        builder.put("-unit", ParameterUnit.table.name(), false);
        builder.put("-template", "resources/template/" + templateFileName + ".txt");
        builder.put("-template.templateGroup", "resources/template/" + templateFileName + ".stg");
    }

    private void addDatasetSrcParams(final ParametersBuilder builder) {
        builder.put("-src.src", DATASET_SRC_DIR);
        builder.put("-src.srcType", this.datasetType.toDataSourceType().name());
        if (this.usesDatasetEncoding()) {
            builder.put("-encoding", this.datasetEncoding);
        }
    }

    private void copyClasspathResource(final String resource, final File dest) throws IOException {
        try (final InputStream is = ScaffoldOption.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String headerNames(final Column[] columns) {
        return Arrays.stream(columns)
                .map(Column::getColumnName)
                .collect(Collectors.joining(","));
    }
}
