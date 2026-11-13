package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.RecordConverter;
import yo.dbunitcli.application.command.generate.GenerateContext;
import yo.dbunitcli.application.command.generate.GenerateStrategy;
import yo.dbunitcli.application.command.generate.GenerateWriter;
import yo.dbunitcli.application.command.generate.XlsxTemplateType;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.EncodingOption;
import yo.dbunitcli.application.option.ResultOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.ParallelTableMapping;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.Workers;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @param resultFile -resultPath未指定時のファイル名
 */
public record GenerateOption(Parameter parameter, String resultFile, String resultDir, String resultPath,
                             GenerateType generateType,
                             ParameterUnit unit, String unitSetting, String unitSettingEncoding, DbOperation operation,
                             String sqlFilePrefix, String sqlFileSuffix,
                             String commit, String template, String outputEncoding, DataSetLoadOption srcData,
                             TemplateRenderOption templateOption, String includeAllColumns, String lazyLoad,
                             String fixedLength, String defaultLength, String align,
                             XlsxTemplateType templateType, String oldTable, String newTable, String keys, String compares, String compareSheetName,
                             String parallel)
        implements CommandLineOption<GenerateDto>, GenerateContext {

    public GenerateOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        this(param, resultFile, dto.getResultDir(), dto.getResultPath(),
             GenerateOption.getGenerateType(dto), GenerateOption.getGenerateType(dto).strategy().isFixedTemplate() ?
                     GenerateOption.getGenerateType(dto).strategy().fixedUnit() :
                     dto.getUnit() != null ? dto.getUnit() : ParameterUnit.record,
             !GenerateOption.getGenerateType(dto).strategy().isFixedTemplate() && Strings.isNotEmpty(dto.getUnitSetting())
                     ? dto.getUnitSetting() : "",
             GenerateOption.getGenerateType(dto).strategy().isFixedTemplate() ? null : dto.getUnitSettingEncoding(),
             dto.getOperation(),
             Strings.isNotEmpty(dto.getSqlFilePrefix()) ? dto.getSqlFilePrefix() : "",
             Strings.isNotEmpty(dto.getSqlFileSuffix()) ? dto.getSqlFileSuffix() : "",
             Strings.isNotEmpty(dto.getCommit()) ? dto.getCommit() : "true", dto.getTemplate(),
             dto.getOutputEncoding(),
             new DataSetLoadOption("src", dto.getSrcData()),
             new TemplateRenderOption("template", dto.getTemplateOption()),
             Strings.isNotEmpty(dto.getIncludeAllColumns()) ? dto.getIncludeAllColumns() : "false",
             Strings.isNotEmpty(dto.getLazyLoad()) ? dto.getLazyLoad() : "true",
             Strings.isNotEmpty(dto.getFixedLength()) ? dto.getFixedLength() : "",
             dto.getDefaultLength(),
             dto.getAlign(),
             dto.getTemplateType() != null ? dto.getTemplateType() : XlsxTemplateType.table,
             Strings.isNotEmpty(dto.getOldTable()) ? dto.getOldTable() : "",
             Strings.isNotEmpty(dto.getNewTable()) ? dto.getNewTable() : "",
             Strings.isNotEmpty(dto.getKeys()) ? dto.getKeys() : "",
             Strings.isNotEmpty(dto.getCompares()) ? dto.getCompares() : "",
             dto.getCompareSheetName(),
             DataSetConverterOption.parallel(dto.getParallel()));
    }

    public static GenerateDto toDto(final String[] args) {
        final GenerateDto dto = new GenerateDto();
        new ArgumentMapper("", CommandLineOption.ARGUMENT_FUNCTION, CommandLineOption.ARGUMENT_FILTER).populate(args,
                                                                                                                dto);
        new ArgumentMapper("src").populate(args, dto.getSrcData());
        new ArgumentMapper("template").populate(args, dto.getTemplateOption());
        return dto;
    }

    @Override
    public String resultPath() {
        return Strings.orDefault(this.resultPath, this.resultFile);
    }

    @Override
    public String unitSettingEncoding() {
        return EncodingOption.orDefault(this.unitSettingEncoding);
    }

    @Override
    public String outputEncoding() {
        return Strings.orDefault(this.outputEncoding, DataSetConverterOption.DEFAULT_OUTPUT_ENCODING);
    }

    private static GenerateType getGenerateType(final GenerateDto dto) {
        return dto.getGenerateType() != null ? dto.getGenerateType() : GenerateType.txt;
    }

    private GenerateStrategy strategy() {
        return this.generateType.strategy();
    }

    /**
     * 出力単位ごとに、出力先とパラメータを渡す。unit=recordは読み込んだ行をその場で出力する（forEachRecord）。
     * unit=tableは、並列化できればテーブルごとに別スレッドで出力する（いずれも処理順は不定）。
     * <ul>
     *     <li>テキスト・Excel（lazyLoad）: 読込から出力までをテーブルごとに行う。並列化できる条件はsrcの並列読込と同じ</li>
     *     <li>Excel（lazyLoadなし）: 読込は全件まとめて済ませ（srcの並列読込の対象）、出力だけを並列にする</li>
     * </ul>
     * 読込タスクまたは出力先を共有するテーブルは同じレーンにまとめ、レーン内は元の順に逐次処理する
     * （タスクは状態を持ちうるため同じタスクを同時に実行せず、同じ出力先は逐次と同じく最後のテーブルの内容が残る）
     */
    public void forEachParameter(final BiConsumer<File, Parameter> action) {
        final Function<Parameter, File> resultFiles = this.resultFiles();
        if (this.unit() == ParameterUnit.record && this.srcData.srcType() != DataSourceType.none) {
            this.forEachRecord(resultFiles, action);
            return;
        }
        if (this.unit() != ParameterUnit.table) {
            try (final Stream<Parameter> parameters = this.parameterStream()) {
                parameters.forEach(it -> action.accept(resultFiles.apply(it), it));
            }
            return;
        }
        final ComparableDataSetLoader loader = this.getComparableDataSetLoader();
        final ComparableDataSetParam param = this.dataSetParam();
        if (this.strategy().isExcel() && !Boolean.parseBoolean(this.lazyLoad)) {
            this.forEachTable(ParameterUnit.loadedTableTemplates(loader, loader.loadDataSet(param)).toList(),
                              param, ParallelTableMapping.parallelism(param) > 1, resultFiles, action);
            return;
        }
        final ComparableDataSetProducer source = loader.getComparableDataSetProducer(param);
        // 行はテーブルごとの出力の中で読むため、全テーブルの出力が終わるまで接続を保つ
        try (final ComparableDataSetProducer producer = this.strategy().wrapProducer(this, source)) {
            final List<ParameterUnit.TableTemplate> templates = this.strategy().isExcel()
                    ? ParameterUnit.streamingTableTemplates(loader, producer).toList()
                    : ParameterUnit.tableTemplates(loader, producer, this.unitTableSeparators()).toList();
            this.forEachTable(templates, param, ParallelTableMapping.isParallelizable(producer), resultFiles, action);
        }
    }

    /**
     * unit=recordは、読み込んだ行をその場で出力する（データセット全体を保持しない。includeMetaDataは読込を終えるまで溜める）。
     * srcの読込は並列化できればレーンごとに並列にし、描画・書込は並列数が2以上なら出力先ごとに決まった作業スレッドで投入順に行う。
     * 別のレーンから同じ出力先へ書く場合、書く順（残る内容）は不定
     */
    private void forEachRecord(final Function<Parameter, File> resultFiles, final BiConsumer<File, Parameter> action) {
        final ComparableDataSetLoader loader = this.getComparableDataSetLoader();
        final ComparableDataSetParam param = this.dataSetParam();
        final int threads = ParallelTableMapping.parallelism(param);
        if (threads < 2) {
            this.loadRecords(loader, param, it -> action.accept(resultFiles.apply(it), it));
            return;
        }
        Workers.forEachByKey(threads, RecordOutput::resultFile
                , it -> action.accept(it.resultFile(), it.parameter())
                , submit -> this.loadRecords(loader, param, it -> submit.accept(new RecordOutput(resultFiles.apply(it), it))));
    }

    private void loadRecords(final ComparableDataSetLoader loader, final ComparableDataSetParam param
            , final Consumer<Parameter> action) {
        loader.loadDataSet(param.toBuilder()
                                .setConverter(new RecordConverter(loader.parameter().asInputParam(), action, param.mapIncludeMetaData()))
                                .build());
    }

    /**
     * dataset（またはsrcのないrecord）の出力単位。全件を読み込んでから出力する（Excelのdataset+lazyLoadは出力の中で読む）
     */
    private Stream<Parameter> parameterStream() {
        return this.strategy().isExcel() && Boolean.parseBoolean(this.lazyLoad)
                ? this.unit().lazyLoadStream(this.getComparableDataSetLoader(), this.dataSetParam())
                : this.unit().dataSetToStream(this.getComparableDataSetLoader(), this.dataSetParam());
    }

    private void forEachTable(final List<ParameterUnit.TableTemplate> templates, final ComparableDataSetParam param,
                              final boolean parallelizable, final Function<Parameter, File> resultFiles,
                              final BiConsumer<File, Parameter> action) {
        // 出力先は行を読む前に決める（行を含むパラメータで描画すると、行を文字列化する分だけ無駄になる）
        final List<TableOutput> outputs = templates.stream()
                .map(it -> new TableOutput(it, resultFiles.apply(it.unloaded())))
                .toList();
        if (parallelizable) {
            final List<List<TableOutput>> lanes = Workers.groupByShared(outputs, outputs.stream()
                    .map(it -> Stream.concat(it.template().tasks(), Stream.of(it.resultFile())).toList())
                    .toList());
            if (lanes.size() > 1) {
                Workers.forEach(ParallelTableMapping.parallelism(param), "generate-", lanes, (lane, resources, first) -> lane.forEach(
                        it -> action.accept(it.resultFile(), (first ? it.template() : it.template().forWorker(resources)).load())));
                return;
            }
        }
        outputs.forEach(it -> action.accept(it.resultFile(), it.template().load()));
    }

    private record TableOutput(ParameterUnit.TableTemplate template, File resultFile) {
    }

    private record RecordOutput(File resultFile, Parameter parameter) {
    }

    public TableSeparators unitTableSeparators() {
        if (Strings.isEmpty(this.unitSetting)) {
            final TableSeparators defaultTableSeparators = this.classpathDefaultTableSeparators();
            if (defaultTableSeparators != null) {
                return defaultTableSeparators;
            }
        }
        try {
            return new FromJsonTableSeparatorsBuilder(this.unitSettingEncoding()).build(this.unitSetting, BaseDir.GENERATE_SETTING);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private TableSeparators classpathDefaultTableSeparators() {
        final String defaultSettings = this.strategy().defaultSettingsPath();
        return defaultSettings == null ? null
                : new FromJsonTableSeparatorsBuilder(StandardCharsets.UTF_8.toString()).loadFromClasspath(defaultSettings).build();
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir());
    }

    /**
     * 出力先の決め方。パスのパターンとレンダラーは1回だけ用意し、出力ごとには描画だけを行う（複数スレッドから呼ばれうる）
     */
    private Function<Parameter, File> resultFiles() {
        final File resultDir = this.getResultDir();
        final TemplateRender render = this.templateOption.getTemplateRender();
        final String pathTemplate = this.strategy().resultPathTemplate(this);
        return param -> new File(resultDir, render.render(pathTemplate, param));
    }

    public GenerateWriter writer() throws IOException {
        return this.strategy().writer(this);
    }

    @Override
    public List<Violation> validate() {
        return Option.concat(this.srcData.validate()
                , this.validateEncoding("-outputEncoding", this.outputEncoding)
                , this.strategy().isFixedTemplate() ? List.of() : this.validateTemplate()
                , this.strategy().validateOption(this)
                , this.validateParallel(this.parallel));
    }

    private List<Violation> validateTemplate() {
        return Option.concat(this.validateRequired("-template", this.template)
                , this.validateFileExists("-template", this.template, BaseDir.TEMPLATE)
                , this.templateOption.validate()
                , this.unit == ParameterUnit.table && !this.strategy().isExcel()
                        ? Option.concat(this.validateFileExists("-unitSetting", this.unitSetting, BaseDir.GENERATE_SETTING)
                        , this.validateEncoding("-unitSettingEncoding", this.unitSettingEncoding))
                        : List.of());
    }

    @Override
    public GenerateDto toDto() {
        return GenerateOption.toDto(this.toArgs(true));
    }

    @Override
    public ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(
                this.parameter().add("commit", Boolean.parseBoolean(this.commit))
                                .add("includeAllColumns", Boolean.parseBoolean(this.includeAllColumns)));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        final ParametersBuilder result = new ParametersBuilder();
        if (this.generateType == null) {
            return result;
        }
        result.put("-generateType", this.generateType, GenerateType.class);
        if (!this.strategy().isFixedTemplate()) {
            result.put("-unit", this.unit, ParameterUnit.class);
        }
        if (this.unit == ParameterUnit.table && !this.strategy().isExcel() && !this.strategy().isFixedTemplate()) {
            result.putFile("-unitSetting", this.unitSetting, BaseDir.GENERATE_SETTING);
            result.putWithDefault("-unitSettingEncoding", this.unitSettingEncoding, EncodingOption.defaultEncoding());
        }
        if (!this.strategy().isFixedTemplate()) {
            result.putFile("-template", this.template, true, BaseDir.TEMPLATE);
        }
        final ParametersBuilder srcComponent = this.srcData.toParametersBuilder();
        if (this.strategy().useJdbcMetaData()) {
            srcComponent.remove("-src.useJdbcMetaData");
        }
        if (!this.strategy().loadData()) {
            srcComponent.remove("-src.loadData");
        }
        this.strategy().putOptionParameters(this, result);
        if (this.isParallelizable()) {
            result.putWithDefault("-parallel", this.parallel, DataSetConverterOption.AUTO_PARALLEL_DESCRIPTION);
        }
        result.addComponent("srcData", srcComponent.build());
        if (!this.strategy().isFixedTemplate()) {
            result.addComponent("templateOption", this.templateOptionArgs());
        }
        result.putDir("-result", this.resultDir, BaseDir.RESULT);
        result.putWithDefault("-resultPath", this.resultPath, ResultOption.RESULT_PATH_DESCRIPTION);
        if (this.strategy().isText()) {
            result.putWithDefault("-outputEncoding", this.outputEncoding, DataSetConverterOption.DEFAULT_OUTPUT_ENCODING);
        }
        return result;
    }

    /**
     * -parallelを表示するか。srcを並列に読み込める形式か、unit=record（出力を並列にする）の場合
     */
    public boolean isParallelizable() {
        return this.srcData.srcType() != null && this.srcData.srcType() != DataSourceType.none
                && (this.srcData.srcType().isParallelizable() || this.unit() == ParameterUnit.record);
    }

    /**
     * @return 並列数。未指定ならnull（並列化できればCPUコア数で自動）
     */
    public Integer parallelism() {
        return DataSetConverterOption.parallelism(this.parallel);
    }

    public ComparableDataSetParam dataSetParam() {
        final ComparableDataSetParam.Builder builder = this.srcData.getParam();
        if (this.strategy().loadData() && Strings.isEmpty(this.srcData.getSetting())) {
            final TableSeparators defaultTableSeparators = this.classpathDefaultTableSeparators();
            if (defaultTableSeparators != null) {
                builder.setTableSeparators(defaultTableSeparators);
            }
        }
        builder.setUseJdbcMetaData(this.strategy().useJdbcMetaData())
               .setLoadData(this.strategy().loadData())
               .setParallel(this.parallelism());
        return builder.build();
    }

    public File getTemplatePath() {
        return FileResources.searchTemplate(this.template());
    }

    private Parameters templateOptionArgs() {
        final ParametersBuilder templateComponent = this.templateOption.toParametersBuilder();
        this.strategy().putTemplateOptionParameters(this, templateComponent);
        return templateComponent.build();
    }
}
