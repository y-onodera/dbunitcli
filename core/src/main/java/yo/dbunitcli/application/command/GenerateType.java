package yo.dbunitcli.application.command;

import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.command.generate.DdlGenerateStrategy;
import yo.dbunitcli.application.command.generate.DescriptorGenerateStrategy;
import yo.dbunitcli.application.command.generate.ExcelGenerateStrategy;
import yo.dbunitcli.application.command.generate.FixedColumnDefGenerateStrategy;
import yo.dbunitcli.application.command.generate.GenerateStrategy;
import yo.dbunitcli.application.command.generate.GenerateWriter;
import yo.dbunitcli.application.command.generate.JavaBeanGenerateStrategy;
import yo.dbunitcli.application.command.generate.SettingsGenerateStrategy;
import yo.dbunitcli.application.command.generate.SqlGenerateStrategy;
import yo.dbunitcli.application.command.generate.TextGenerateStrategy;
import yo.dbunitcli.application.command.generate.XlsxSchemaGenerateStrategy;
import yo.dbunitcli.application.command.generate.XlsxTemplateGenerateStrategy;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.io.IOException;
import java.util.List;

/**
 * -generateTypeの値。具体的な生成ロジックは定数ごとの{@link GenerateStrategy}実装（command/generate/配下）が持ち、
 * このenumは委譲するだけ
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

    public String getStgPath() {
        return this.strategy.stgPath();
    }

    public String getTemplatePath() {
        return this.strategy.templatePath();
    }

    public STGroup getStGroup() {
        return this.strategy.stGroup();
    }

    public String getTemplateString(final GenerateOption option) {
        return this.strategy.templateString(option);
    }

    public String defaultSettingsPath() {
        return this.strategy.defaultSettingsPath();
    }

    public boolean isExcel() {
        return this.strategy.isExcel();
    }

    public boolean isText() {
        return this.strategy.isText();
    }

    protected boolean isFixedTemplate() {
        return this.strategy.isFixedTemplate();
    }

    protected ParameterUnit getFixedUnit() {
        return this.strategy.fixedUnit();
    }

    protected boolean loadData() {
        return this.strategy.loadData();
    }

    protected boolean useJdbcMetaData() {
        return this.strategy.useJdbcMetaData();
    }

    protected ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
        return this.strategy.wrapProducer(option, producer);
    }

    protected String resultPathTemplate(final GenerateOption option) {
        return this.strategy.resultPathTemplate(option);
    }

    protected List<Option.Violation> validateOption(final GenerateOption option) {
        return this.strategy.validateOption(option);
    }

    protected void putOptionParameters(final GenerateOption option, final Option.ParametersBuilder builder) {
        this.strategy.putOptionParameters(option, builder);
    }

    protected void putTemplateOptionParameters(final GenerateOption option, final Option.ParametersBuilder builder) {
        this.strategy.putTemplateOptionParameters(option, builder);
    }

    /**
     * テンプレートの読込・コンパイルを1回だけ行い、出力処理を返す。戻り値は複数スレッドから同時に呼ばれうる
     */
    public GenerateWriter writer(final GenerateOption option) throws IOException {
        return this.strategy.writer(option);
    }

    /**
     * 記述子行を合成する型（ddl/javaBean/xlsxSchema/fixedColumnDef）のstrategy。ScaffoldTargetが記述子dataset雛型の合成に使う
     *
     * @throws IllegalStateException 記述子型でない場合
     */
    DescriptorGenerateStrategy descriptorStrategy() {
        if (this.strategy instanceof final DescriptorGenerateStrategy descriptor) {
            return descriptor;
        }
        throw new IllegalStateException(this.name() + " is not descriptor generateType");
    }
}
