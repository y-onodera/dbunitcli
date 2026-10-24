package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.RecordConverter;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public record GenerateOption(Parameter parameter, String resultDir, String resultPath, GenerateType generateType,
                             ParameterUnit unit, String unitSetting, String unitSettingEncoding, DbOperation operation,
                             String sqlFilePrefix, String sqlFileSuffix,
                             boolean commit, String template, String outputEncoding, DataSetLoadOption srcData,
                             TemplateRenderOption templateOption, boolean includeAllColumns, boolean lazyLoad,
                             String fixedLength, int defaultLength, String align, String parallel)
        implements CommandLineOption<GenerateDto> {

    public GenerateOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        this(param, dto.getResultDir(),
             Optional.ofNullable(dto.getResultPath()).filter(it -> !it.isEmpty()).orElse(resultFile),
             GenerateOption.getGenerateType(dto), GenerateOption.getGenerateType(dto).isFixedTemplate() ?
                     GenerateOption.getGenerateType(dto).getFixedUnit() :
                     dto.getUnit() != null ? dto.getUnit() : ParameterUnit.record,
             !GenerateOption.getGenerateType(dto).isFixedTemplate() && Strings.isNotEmpty(dto.getUnitSetting())
                     ? dto.getUnitSetting() : "",
             !GenerateOption.getGenerateType(dto).isFixedTemplate() && Strings.isNotEmpty(dto.getUnitSettingEncoding())
                     ? dto.getUnitSettingEncoding()
                     : Charset.defaultCharset().displayName(),
             dto.getOperation(),
             Strings.isNotEmpty(dto.getSqlFilePrefix()) ? dto.getSqlFilePrefix() : "",
             Strings.isNotEmpty(dto.getSqlFileSuffix()) ? dto.getSqlFileSuffix() : "",
             !Strings.isNotEmpty(dto.getCommit()) || Boolean.parseBoolean(dto.getCommit()), dto.getTemplate(),
             Strings.isNotEmpty(dto.getOutputEncoding()) ? dto.getOutputEncoding() : "UTF-8",
             new DataSetLoadOption("src", dto.getSrcData()),
             new TemplateRenderOption("template", dto.getTemplateOption()),
             Strings.isNotEmpty(dto.getIncludeAllColumns()) && Boolean.parseBoolean(dto.getIncludeAllColumns()),
             !Strings.isNotEmpty(dto.getLazyLoad()) || Boolean.parseBoolean(dto.getLazyLoad()),
             Strings.isNotEmpty(dto.getFixedLength()) ? dto.getFixedLength() : "",
             Strings.isNotEmpty(dto.getDefaultLength()) ? Integer.parseInt(dto.getDefaultLength()) : 10,
             Strings.isNotEmpty(dto.getAlign()) ? dto.getAlign() : "left",
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

    private static GenerateType getGenerateType(final GenerateDto dto) {
        return dto.getGenerateType() != null ? dto.getGenerateType() : GenerateType.txt;
    }

    public String templateString() {
        return this.generateType.getTemplateString(this);
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
        if (this.unit() == ParameterUnit.record && this.srcData.srcType() != DataSourceType.none) {
            this.forEachRecord(action);
            return;
        }
        if (this.unit() != ParameterUnit.table) {
            try (final Stream<Parameter> parameters = this.parameterStream()) {
                parameters.forEach(it -> action.accept(this.resultFile(it), it));
            }
            return;
        }
        final ComparableDataSetLoader loader = this.getComparableDataSetLoader();
        final ComparableDataSetParam param = this.dataSetParam();
        if (this.generateType.isExcel() && !this.lazyLoad) {
            this.forEachTable(ParameterUnit.loadedTableTemplates(loader, loader.loadDataSet(param)).toList(),
                              param, ParallelTableMapping.parallelism(param) > 1, action);
            return;
        }
        final ComparableDataSetProducer source = loader.getComparableDataSetProducer(param);
        // 行はテーブルごとの出力の中で読むため、全テーブルの出力が終わるまで接続を保つ
        try (final ComparableDataSetProducer producer = this.generateType.isExcel()
                ? source
                : this.generateType().wrapProducer(this, source)) {
            final List<ParameterUnit.TableTemplate> templates = this.generateType.isExcel()
                    ? ParameterUnit.streamingTableTemplates(loader, producer).toList()
                    : ParameterUnit.tableTemplates(loader, producer, this.unitTableSeparators()).toList();
            this.forEachTable(templates, param, ParallelTableMapping.isParallelizable(producer), action);
        }
    }

    /**
     * unit=recordは、読み込んだ行をその場で出力する（データセット全体を保持しない。includeMetaDataは読込を終えるまで溜める）。
     * srcの読込は並列化できればレーンごとに並列にし、描画・書込は並列数が2以上なら出力先ごとに決まった作業スレッドで投入順に行う。
     * 別のレーンから同じ出力先へ書く場合、書く順（残る内容）は不定
     */
    private void forEachRecord(final BiConsumer<File, Parameter> action) {
        final ComparableDataSetLoader loader = this.getComparableDataSetLoader();
        final ComparableDataSetParam param = this.dataSetParam();
        final int threads = ParallelTableMapping.parallelism(param);
        if (threads < 2) {
            this.loadRecords(loader, param, it -> action.accept(this.resultFile(it), it));
            return;
        }
        ParallelTableMapping.<RecordOutput>forEachByKey(threads, RecordOutput::resultFile
                , it -> action.accept(it.resultFile(), it.parameter())
                , submit -> this.loadRecords(loader, param, it -> submit.accept(new RecordOutput(this.resultFile(it), it))));
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
        return this.generateType.isExcel() && this.lazyLoad
                ? this.unit().lazyLoadStream(this.getComparableDataSetLoader(), this.dataSetParam())
                : this.unit().dataSetToStream(this.getComparableDataSetLoader(), this.dataSetParam());
    }

    private void forEachTable(final List<ParameterUnit.TableTemplate> templates, final ComparableDataSetParam param,
                              final boolean parallelizable, final BiConsumer<File, Parameter> action) {
        // 出力先は行を読む前に決める（行を含むパラメータで描画すると、行を文字列化する分だけ無駄になる）
        final List<TableOutput> outputs = templates.stream()
                .map(it -> new TableOutput(it, this.resultFile(it.unloaded())))
                .toList();
        if (parallelizable) {
            final List<List<TableOutput>> lanes = ParallelTableMapping.groupByShared(outputs, outputs.stream()
                    .map(it -> Stream.concat(it.template().tasks(), Stream.of(it.resultFile())).toList())
                    .toList());
            if (lanes.size() > 1) {
                ParallelTableMapping.forEach(param, lanes, (lane, resources, first) -> lane.forEach(
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
            return new FromJsonTableSeparatorsBuilder(this.unitSettingEncoding).build(this.unitSetting);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    private TableSeparators classpathDefaultTableSeparators() {
        final String defaultSettings = this.generateType().defaultSettingsPath();
        return defaultSettings == null ? null
                : new FromJsonTableSeparatorsBuilder(StandardCharsets.UTF_8.toString()).loadFromClasspath(defaultSettings).build();
    }

    public File resultFile(final Parameter param) {
        return new File(this.getResultDir(), this.resultPath(param));
    }

    public File getResultDir() {
        return FileResources.resultDir(this.resultDir());
    }

    public String resultPath(final Parameter param) {
        return this.templateOption.getTemplateRender().render(this.generateType().resultPathTemplate(this), param);
    }

    public GenerateType.Writer writer() throws IOException {
        return this.generateType().writer(this);
    }

    @Override
    public GenerateDto toDto() {
        return GenerateOption.toDto(this.toArgs(true));
    }

    @Override
    public ComparableDataSetLoader getComparableDataSetLoader() {
        return new ComparableDataSetLoader(
                this.parameter().add("commit", this.commit).add("includeAllColumns", this.includeAllColumns));
    }

    @Override
    public ParametersBuilder toParametersBuilder() {
        final ParametersBuilder result = new ParametersBuilder();
        if (this.generateType == null) {
            return result;
        }
        result.put("-generateType", this.generateType, GenerateType.class);
        if (!this.generateType.isFixedTemplate()) {
            result.put("-unit", this.unit, ParameterUnit.class);
        }
        if (this.unit == ParameterUnit.table && !this.generateType.isExcel() && !this.generateType.isFixedTemplate()) {
            result.putFile("-unitSetting", this.unitSetting, BaseDir.SETTING);
            result.put("-unitSettingEncoding", this.unitSettingEncoding);
        }
        if (!this.generateType.isFixedTemplate()) {
            result.putFile("-template", this.template, true, BaseDir.TEMPLATE);
        }
        final ParametersBuilder srcComponent = this.srcData.toParametersBuilder();
        if (this.generateType.useJdbcMetaData()) {
            srcComponent.remove("-src.useJdbcMetaData");
        }
        if (!this.generateType.loadData()) {
            srcComponent.remove("-src.loadData");
        }
        switch (this.generateType) {
            case sql -> result.put("-commit", Boolean.toString(this.commit)).put("-op", this.operation, DbOperation.class)
                              .put("-sqlFilePrefix", this.sqlFilePrefix).put("-sqlFileSuffix", this.sqlFileSuffix);
            case ddl -> result.put("-sqlFilePrefix", this.sqlFilePrefix).put("-sqlFileSuffix", this.sqlFileSuffix);
            case fixedColumnDef -> result.put("-fixedLength", this.fixedLength)
                                          .put("-defaultLength", Integer.toString(this.defaultLength)).put("-align", this.align);
            case settings -> result.put("-includeAllColumns", Boolean.toString(this.includeAllColumns));
            case xlsx, xls -> result.put("-lazyLoad", Boolean.toString(this.lazyLoad));
            default -> {
            }
        }
        if (this.isParallelizable()) {
            result.put("-parallel", this.parallel);
        }
        result.addComponent("srcData", srcComponent.build());
        if (!this.generateType.isFixedTemplate()) {
            result.addComponent("templateOption", this.templateOptionArgs());
        }
        result.putDir("-result", this.resultDir, BaseDir.RESULT).put("-resultPath", this.resultPath);
        if (this.generateType.isText()) {
            result.put("-outputEncoding", this.outputEncoding);
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
        if (this.generateType().loadData() && Strings.isEmpty(this.srcData.getSetting())) {
            final TableSeparators defaultTableSeparators = this.classpathDefaultTableSeparators();
            if (defaultTableSeparators != null) {
                builder.setTableSeparators(defaultTableSeparators);
            }
        }
        builder.setUseJdbcMetaData(this.generateType().useJdbcMetaData())
               .setLoadData(this.generateType().loadData())
               .setParallel(this.parallelism());
        return builder.build();
    }

    public File getTemplatePath() {
        return FileResources.searchTemplate(this.template());
    }

    private Parameters templateOptionArgs() {
        final ParametersBuilder templateComponent = this.templateOption.toParametersBuilder();
        if (this.generateType.isExcel()) {
            if (this.generateType == GenerateType.xlsx) {
                templateComponent.put("-formulaProcess", this.templateOption.formulaProcess());
            }
            templateComponent.put("-evaluateFormulas", this.templateOption.evaluateFormulas());
            templateComponent.put("-forceFormulaRecalc", this.templateOption.forceFormulaRecalc());
            templateComponent.put("-fastFormulaProcess", this.templateOption.fastFormulaProcess());
            templateComponent.put("-deleteBlankCells", this.templateOption.deleteBlankCells());
        }
        return templateComponent.build();
    }
}
