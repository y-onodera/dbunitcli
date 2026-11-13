package yo.dbunitcli.application.command.generate;

import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.option.TemplateRenderOption;
import yo.dbunitcli.dataset.DbOperation;

import java.io.File;

/**
 * {@link GenerateStrategy}が参照するGenerateの設定値。GenerateOptionが実装する。
 * strategyをGenerateOption（commandパッケージ）に依存させないための境界で、
 * command.generateパッケージはcommandパッケージを参照しない（依存はcommand→command.generateの一方向）。
 * 検証ヘルパー（validateBoolean等）は{@link Option}のdefaultメソッドを使う
 */
public interface GenerateContext extends Option, DescriptorOption {

    String resultPath();

    ParameterUnit unit();

    DbOperation operation();

    String sqlFilePrefix();

    String sqlFileSuffix();

    String commit();

    String template();

    String outputEncoding();

    TemplateRenderOption templateOption();

    String includeAllColumns();

    String lazyLoad();

    XlsxTemplateType templateType();

    String keys();

    String compares();

    String compareSheetName();

    /** -templateのファイル */
    File getTemplatePath();
}
