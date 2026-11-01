package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateRender;

import java.io.IOException;
import java.nio.file.Files;

/**
 * xlsx/xls: ユーザー指定の-template（Excel）をjxlsで描画する型
 */
public final class ExcelGenerateStrategy implements GenerateStrategy {

    @Override
    public boolean isExcel() {
        return true;
    }

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public GenerateWriter writer(final GenerateOption option) throws IOException {
        final JxlsTemplateRender render = JxlsTemplateRender.builder()
                .setTemplateParameterAttribute(option.templateOption().templateParameterAttribute())
                .setFormulaProcess(Boolean.parseBoolean(option.templateOption().formulaProcess()))
                .setEvaluateFormulas(Boolean.parseBoolean(option.templateOption().evaluateFormulas()))
                .setForceFormulaRecalc(Boolean.parseBoolean(option.templateOption().forceFormulaRecalc()))
                .setFastFormulaProcess(Boolean.parseBoolean(option.templateOption().fastFormulaProcess()))
                .setDeleteBlankCells(Boolean.parseBoolean(option.templateOption().deleteBlankCells())).build();
        final byte[] template = Files.readAllBytes(option.getTemplatePath().toPath());
        final boolean useStreamingEach = option.unit() != ParameterUnit.record;
        return (resultFile, param) -> render.render(template, resultFile, param, useStreamingEach);
    }
}
