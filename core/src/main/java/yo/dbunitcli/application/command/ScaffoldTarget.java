package yo.dbunitcli.application.command;

import org.dbunit.dataset.Column;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.command.generate.DescriptorGenerateStrategy;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.util.Arrays;

/**
 * Scaffoldの-targetごとの差分を集約するenum。4target共通のフロー（unitSetting/template/dataset/parameterの
 * 各雛型を対応オプション指定時のみ独立出力）は{@link ScaffoldOption}が担い、targetごとに異なる
 * 「対応するGenerateType・サンプルunitSetting」だけをここで定義する
 * （記述子datasetの列定義・producer・出力ファイル名はGenerateTypeのDescriptorGenerateStrategyに委譲する）。
 */
enum ScaffoldTarget {
    ddl(GenerateType.ddl),
    javaBean(GenerateType.javaBean),
    xlsxSchema(GenerateType.xlsxSchema),
    fixedColumnDef(GenerateType.fixedColumnDef) {
        // GenerateType.fixedColumnDefはdefaultSettingsPath()を持たないため、Scaffold専用のサンプルを使う
        @Override
        String sampleUnitSettingPath() {
            return "fixedcolumndef/fixedColumnDefSettings.json";
        }
    };

    private final GenerateType generateType;

    // 記述子型でないGenerateTypeを渡すとクラス初期化時に失敗する
    private final DescriptorGenerateStrategy descriptor;

    ScaffoldTarget(final GenerateType generateType) {
        this.generateType = generateType;
        if (!(generateType.strategy() instanceof final DescriptorGenerateStrategy descriptor)) {
            throw new IllegalArgumentException(generateType + " is not descriptor generateType");
        }
        this.descriptor = descriptor;
    }

    static ScaffoldTarget fromString(final String target) {
        return Arrays.stream(ScaffoldTarget.values())
                     .filter(it -> it.name().equals(target))
                     .findFirst()
                     .orElse(null);
    }

    GenerateType generateType() {
        return this.generateType;
    }

    String stgPath() {
        return this.descriptor.stgPath();
    }

    String templatePath() {
        return this.descriptor.templatePath();
    }

    /**
     * unitSetting雛型としてコピーするclasspathリソース（既定はGenerateTypeのdefaultSettingsPath()）。
     * 利用者が他のデータソースの列をテンプレートの読む列へマッピングする際のヒントとなるよう、
     * 各サンプルはテンプレートで利用される全列を定義しておくこと
     */
    String sampleUnitSettingPath() {
        return this.descriptor.defaultSettingsPath();
    }

    /** 記述子datasetの列定義（headerless出力時の-headerName用） */
    Column[] datasetSchema() {
        return this.descriptor.descriptorSchema();
    }

    /** 記述子dataset合成用producer。Generate側のwrapProducer()と同じDescriptorGenerateStrategy#wrapDescriptor()を使う */
    ComparableDataSetProducerWrapper wrapProducer(final ScaffoldOption option, final ComparableDataSetProducer source) {
        return this.descriptor.wrapDescriptor(option, source);
    }

    /** カスタムテンプレート（generateType=txt）駆動時の-resultPathパターン。builtin駆動と同じファイル名にする */
    String customTemplateResultPath() {
        return this.descriptor.tableFileName(new TemplateRender.Builder().build());
    }

    /** 組み込みgenerateType駆動の.paramに追記するtarget固有オプション（Generate側の記述子オプション出力と共有） */
    void putBuiltinExtraParams(final ScaffoldOption option, final Option.ParametersBuilder builder) {
        this.descriptor.putDescriptorParameters(option, builder);
    }
}
