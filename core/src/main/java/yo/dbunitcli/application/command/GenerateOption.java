package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.option.DataSetConverterOption;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.ParallelTableMapping;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    public Stream<Parameter> parameterStream() {
        if (this.generateType.isExcel()) {
            return this.lazyLoad
                    ? this.unit().lazyLoadStream(this.getComparableDataSetLoader(), this.dataSetParam())
                    : this.unit().dataSetToStream(this.getComparableDataSetLoader(), this.dataSetParam());
        }
        final ComparableDataSetLoader loader = this.getComparableDataSetLoader();
        if (this.unit() != ParameterUnit.table) {
            // record/datasetは全件を読み込んでから出力する（producerは読込後に閉じる）
            return this.unit().dataSetToStream(loader, this.dataSetParam());
        }
        final ComparableDataSetProducer producer = this.generateType()
                .wrapProducer(this, loader.getComparableDataSetProducer(this.dataSetParam()));
        return this.unit().templateStream(loader, producer, this.unitTableSeparators()).onClose(producer::close);
    }

    /**
     * 出力単位のパラメータを順に渡す。unit=tableは、並列化できればテーブルごとに別スレッドで出力する（処理順は不定）。
     * <ul>
     *     <li>テキスト・Excel（lazyLoad）: 読込から出力までをテーブルごとに行う。並列化できる条件はsrcの並列読込と同じ</li>
     *     <li>Excel（lazyLoadなし）: 読込は全件まとめて済ませ（srcの並列読込の対象）、出力だけを並列にする</li>
     * </ul>
     * いずれも出力先がテーブルごとに異なる場合に限る。読込タスクを共有するテーブル（1つのソースから複数のテーブルを作る場合）は
     * 同じレーンにまとめ、レーン内は元の順に逐次処理する（タスクは状態を持ちうるため、同じタスクを同時に実行しない）
     */
    public void forEachParameter(final Consumer<Parameter> action) {
        if (this.unit() != ParameterUnit.table) {
            try (final Stream<Parameter> parameters = this.parameterStream()) {
                parameters.forEach(action);
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
        // 行はテーブルごとの出力の中で読むため、全テーブルの出力が終わるまで接続を保つ
        try (final ComparableDataSetProducer producer = this.generateType.isExcel()
                ? loader.getComparableDataSetProducer(param)
                : this.generateType().wrapProducer(this, loader.getComparableDataSetProducer(param))) {
            final List<ParameterUnit.TableTemplate> templates = this.generateType.isExcel()
                    ? ParameterUnit.streamingTableTemplates(loader, producer).toList()
                    : ParameterUnit.tableTemplates(loader, producer, this.unitTableSeparators()).toList();
            this.forEachTable(templates, param, ParallelTableMapping.isParallelizable(producer), action);
        }
    }

    private void forEachTable(final List<ParameterUnit.TableTemplate> templates, final ComparableDataSetParam param,
                              final boolean parallelizable, final Consumer<Parameter> action) {
        final List<List<ParameterUnit.TableTemplate>> lanes = ParallelTableMapping.groupByShared(templates,
                templates.stream().map(it -> it.tasks().toList()).toList());
        if (lanes.size() < 2 || !parallelizable || this.hasSameResultFile(templates)) {
            templates.forEach(it -> action.accept(it.load()));
            return;
        }
        ParallelTableMapping.forEach(param, lanes,
                                     (lane, resources, first) -> lane.forEach(
                                             it -> action.accept((first ? it : it.forWorker(resources)).load())));
    }

    /**
     * 同じファイルへ出力するテーブルがあると、並列では最後に書くテーブルが不定になる
     */
    private boolean hasSameResultFile(final List<ParameterUnit.TableTemplate> templates) {
        final Set<String> resultFiles = new HashSet<>();
        return !templates.stream().allMatch(it -> resultFiles.add(this.resultFile(it.unloaded()).getAbsolutePath()));
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
     * -parallelを表示するか。srcを並列に読み込める形式の場合
     */
    public boolean isParallelizable() {
        return this.srcData.srcType() != null && this.srcData.srcType().isParallelizable();
    }

    /**
     * @return 並列数。未指定ならnull（並列化できればCPUコア数で自動）
     */
    public Integer parallelism() {
        return Strings.isEmpty(this.parallel) ? null : Integer.valueOf(this.parallel);
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
