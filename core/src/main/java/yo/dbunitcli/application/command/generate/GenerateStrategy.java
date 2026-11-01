package yo.dbunitcli.application.command.generate;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.IOException;
import java.util.List;

/**
 * generateTypeごとの具体的な生成ロジック。{@link yo.dbunitcli.application.command.GenerateType}の各定数が
 * 実装を1つ保持し、全メソッドを委譲する。default実装は汎用テキスト生成（txt）の振る舞い。
 * <p>
 * 設計4軸との対応: データロード=loadData()/useJdbcMetaData()、テンプレート差し替え可否=isFixedTemplate()、
 * unit=fixedUnit()、出力ファイル形状=resultPathTemplate()
 */
public interface GenerateStrategy {

    /** 組み込みテンプレートの.stg（classpath）。なければnull */
    default String stgPath() {
        return null;
    }

    /** 組み込みテンプレートの本体（classpath）。なければnull */
    default String templatePath() {
        return null;
    }

    default STGroup stGroup() {
        final String stgPath = this.stgPath();
        return stgPath != null
                ? new TemplateRender.Builder().setTemplateParameterAttribute(null).build().createSTGroup(stgPath)
                : null;
    }

    default String templateString(final GenerateOption option) {
        final String templatePath = this.templatePath();
        return templatePath != null ? FileResources.readClasspathResource(templatePath) : null;
    }

    /** unitSetting未指定時に使う組み込み設定（classpath）。なければnull */
    default String defaultSettingsPath() {
        return null;
    }

    default boolean isExcel() {
        return false;
    }

    default boolean isText() {
        return true;
    }

    default boolean isFixedTemplate() {
        return false;
    }

    /** isFixedTemplate()=trueのとき強制するunit */
    default ParameterUnit fixedUnit() {
        return null;
    }

    default boolean loadData() {
        return true;
    }

    default boolean useJdbcMetaData() {
        return false;
    }

    // GenerateOption.forEachParameter()のunit=table（Excel以外）だけがこの戻り値を消費する(ParameterUnit.tableTemplates()経由)。
    // ここをoverrideしてもunit=table以外では反映されない。
    default ComparableDataSetProducer wrapProducer(final GenerateOption option, final ComparableDataSetProducer producer) {
        return producer;
    }

    default String resultPathTemplate(final GenerateOption option) {
        return option.resultPath();
    }

    /** generateType固有オプションの検証（GenerateOption.validate()が共通の検証に加えて呼ぶ） */
    default List<Option.Violation> validateOption(final GenerateOption option) {
        return List.of();
    }

    /** generateType固有オプションをパラメータに出す（GenerateOption.toParametersBuilder()が呼ぶ。出す項目と順序がフォームの表示になる） */
    default void putOptionParameters(final GenerateOption option, final Option.ParametersBuilder builder) {
    }

    /** -template.*のうちgenerateType固有の項目をパラメータに出す（isFixedTemplate()=falseの型でのみ呼ばれる） */
    default void putTemplateOptionParameters(final GenerateOption option, final Option.ParametersBuilder builder) {
    }

    /**
     * テンプレートの読込・コンパイルを1回だけ行い、出力処理を返す。戻り値は複数スレッドから同時に呼ばれうる
     */
    default GenerateWriter writer(final GenerateOption option) throws IOException {
        final TemplateRender render = option.templateOption().getTemplateRender();
        final ST template = render.compile(this.stGroup(), option.templateString());
        return (resultFile, param) -> render.write(template, param, resultFile, option.outputEncoding());
    }
}
