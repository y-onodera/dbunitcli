package yo.dbunitcli.application.command;

import yo.dbunitcli.application.command.generate.DdlGenerateStrategy;
import yo.dbunitcli.application.command.generate.ExcelGenerateStrategy;
import yo.dbunitcli.application.command.generate.FixedColumnDefGenerateStrategy;
import yo.dbunitcli.application.command.generate.GenerateStrategy;
import yo.dbunitcli.application.command.generate.JavaBeanGenerateStrategy;
import yo.dbunitcli.application.command.generate.SettingsGenerateStrategy;
import yo.dbunitcli.application.command.generate.SqlGenerateStrategy;
import yo.dbunitcli.application.command.generate.TextGenerateStrategy;
import yo.dbunitcli.application.command.generate.XlsxSchemaGenerateStrategy;
import yo.dbunitcli.application.command.generate.XlsxTemplateGenerateStrategy;

/**
 * -generateTypeの値。具体的な生成ロジックは定数ごとの{@link GenerateStrategy}実装（command/generate/配下）が持ち、
 * 利用側は{@link #strategy()}を通して呼ぶ
 */
public enum GenerateType {
    txt(new TextGenerateStrategy()),
    xlsx(ExcelGenerateStrategy.xlsx()),
    xls(ExcelGenerateStrategy.xls()),
    settings(new SettingsGenerateStrategy()),
    sql(new SqlGenerateStrategy()),
    ddl(new DdlGenerateStrategy()),
    xlsxTemplate(new XlsxTemplateGenerateStrategy()),
    xlsxSchema(new XlsxSchemaGenerateStrategy()),
    javaBean(new JavaBeanGenerateStrategy()),
    fixedColumnDef(new FixedColumnDefGenerateStrategy());

    private final GenerateStrategy strategy;

    GenerateType(final GenerateStrategy strategy) {
        this.strategy = strategy;
    }

    public GenerateStrategy strategy() {
        return this.strategy;
    }
}
