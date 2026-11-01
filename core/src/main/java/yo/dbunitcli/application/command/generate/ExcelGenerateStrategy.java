package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateRender;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * xlsx/xls: ユーザー指定の-template（Excel）をjxlsで描画する型
 */
public final class ExcelGenerateStrategy implements GenerateStrategy {

    private final boolean supportsFormulaProcess;

    private ExcelGenerateStrategy(final boolean supportsFormulaProcess) {
        this.supportsFormulaProcess = supportsFormulaProcess;
    }

    /** -template.formulaProcessを扱う */
    public static ExcelGenerateStrategy xlsx() {
        return new ExcelGenerateStrategy(true);
    }

    /** -template.formulaProcessを扱わない */
    public static ExcelGenerateStrategy xls() {
        return new ExcelGenerateStrategy(false);
    }

    @Override
    public boolean isExcel() {
        return true;
    }

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public List<Option.Violation> validateOption(final GenerateContext option) {
        return option.validateBoolean("-lazyLoad", option.lazyLoad());
    }

    @Override
    public void putOptionParameters(final GenerateContext option, final Option.ParametersBuilder builder) {
        builder.put("-lazyLoad", option.lazyLoad());
    }

    @Override
    public void putTemplateOptionParameters(final GenerateContext option, final Option.ParametersBuilder builder) {
        final TemplateRenderOption templateOption = option.templateOption();
        if (this.supportsFormulaProcess) {
            builder.putFlag("-formulaProcess", templateOption.formulaProcess());
        }
        builder.putFlag("-evaluateFormulas", templateOption.evaluateFormulas());
        builder.putFlag("-forceFormulaRecalc", templateOption.forceFormulaRecalc());
        builder.putFlag("-fastFormulaProcess", templateOption.fastFormulaProcess());
        builder.putFlag("-deleteBlankCells", templateOption.deleteBlankCells());
    }

    @Override
    public GenerateWriter writer(final GenerateContext option) throws IOException {
        final TemplateRenderOption templateOption = option.templateOption();
        final JxlsTemplateRender render = JxlsTemplateRender.builder()
                .setTemplateParameterAttribute(templateOption.templateParameterAttribute())
                .setFormulaProcess(Boolean.parseBoolean(templateOption.formulaProcess()))
                .setEvaluateFormulas(Boolean.parseBoolean(templateOption.evaluateFormulas()))
                .setForceFormulaRecalc(Boolean.parseBoolean(templateOption.forceFormulaRecalc()))
                .setFastFormulaProcess(Boolean.parseBoolean(templateOption.fastFormulaProcess()))
                .setDeleteBlankCells(Boolean.parseBoolean(templateOption.deleteBlankCells())).build();
        final byte[] template = Files.readAllBytes(option.getTemplatePath().toPath());
        final boolean useStreamingEach = option.unit() != ParameterUnit.record;
        return (resultFile, param) -> render.render(template, resultFile, param, useStreamingEach);
    }
}
