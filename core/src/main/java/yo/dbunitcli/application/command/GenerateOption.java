package yo.dbunitcli.application.command;

import yo.dbunitcli.Strings;
import yo.dbunitcli.application.ArgumentMapper;
import yo.dbunitcli.application.CommandLineOption;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.application.option.DataSetLoadOption;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Stream;

public record GenerateOption(Parameter parameter, String resultDir, String resultPath, GenerateType generateType,
                             ParameterUnit unit, String unitSetting, String unitSettingEncoding, DbOperation operation,
                             String sqlFilePrefix, String sqlFileSuffix,
                             boolean commit, String template, String outputEncoding, DataSetLoadOption srcData,
                             TemplateRenderOption templateOption, boolean includeAllColumns, boolean lazyLoad,
                             String fixedLength, int defaultLength, String align)
        implements CommandLineOption<GenerateDto> {

    public GenerateOption(final String resultFile, final GenerateDto dto, final Parameter param) {
        this(param, dto.getResultDir(),
             Optional.ofNullable(dto.getResultPath()).filter(it -> !it.isEmpty()).orElse(resultFile),
             GenerateOption.getGenerateType(dto), GenerateOption.getGenerateType(dto).isFixedTemplate() ?
                     GenerateOption.getGenerateType(dto).getFixedUnit() :
                     dto.getUnit() != null ? dto.getUnit() : ParameterUnit.record,
             Strings.isNotEmpty(dto.getUnitSetting()) ? dto.getUnitSetting() : "",
             Strings.isNotEmpty(dto.getUnitSettingEncoding())
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
             Strings.isNotEmpty(dto.getAlign()) ? dto.getAlign() : "left");
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
        final ComparableDataSetProducer producer = this.generateType()
                .wrapProducer(this, loader.getComparableDataSetProducer(this.dataSetParam()));
        return this.unit().templateStream(loader, producer,
                                          this.unit() == ParameterUnit.table ? this.unitTableSeparators() : TableSeparators.NONE);
    }

    public TableSeparators unitTableSeparators() {
        if (Strings.isEmpty(this.unitSetting)) {
            final TableSeparators defaultTableSeparators = this.classpathDefaultTableSeparators(this.unitSettingEncoding);
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

    private TableSeparators classpathDefaultTableSeparators(final String settingEncoding) {
        final String defaultSettings = this.generateType().defaultSettingsPath();
        return defaultSettings == null ? null
                : new FromJsonTableSeparatorsBuilder(settingEncoding).loadFromClasspath(defaultSettings).build();
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

    public void write(final File resultFile, final Parameter param) throws IOException {
        this.generateType().write(this, resultFile, param);
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
        if (this.unit == ParameterUnit.table && !this.generateType.isExcel()) {
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

    public ComparableDataSetParam dataSetParam() {
        final ComparableDataSetParam.Builder builder = this.srcData.getParam();
        if (Strings.isEmpty(this.srcData.getSetting())) {
            final TableSeparators defaultTableSeparators = this.classpathDefaultTableSeparators(this.srcData.settingEncoding());
            if (defaultTableSeparators != null) {
                builder.setTableSeparators(defaultTableSeparators);
            }
        }
        builder.setUseJdbcMetaData(this.generateType().useJdbcMetaData())
               .setLoadData(this.generateType().loadData());
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
