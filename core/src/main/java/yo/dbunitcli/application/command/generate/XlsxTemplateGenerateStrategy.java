package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.resource.poi.jxls.JxlsTemplateGenerator;

/**
 * xlsxTemplate: データセットのメタデータからjxlsテンプレート（Excel）をコードで組み立てる型（テンプレート不要）
 */
public final class XlsxTemplateGenerateStrategy implements GenerateStrategy {

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public boolean isFixedTemplate() {
        return true;
    }

    @Override
    public ParameterUnit fixedUnit() {
        return ParameterUnit.dataset;
    }

    @Override
    public boolean loadData() {
        return false;
    }

    @Override
    public GenerateWriter writer(final GenerateContext option) {
        return JxlsTemplateGenerator::createTemplate;
    }
}
