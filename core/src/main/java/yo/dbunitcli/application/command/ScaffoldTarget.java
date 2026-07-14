package yo.dbunitcli.application.command;

import org.dbunit.dataset.Column;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.producer.ComparableDdlMetaDataProducer;
import yo.dbunitcli.dataset.producer.ComparableFixedColumnDefMetaDataProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxSchemaMetaDataProducer;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.util.Arrays;

/**
 * Scaffoldの-targetごとの差分を集約するenum。4target共通のフロー（unitSetting/template/dataset/parameterの
 * 各雛型を対応オプション指定時のみ独立出力）は{@link ScaffoldOption}が担い、targetごとに異なる
 * 「対応するGenerateType・サンプルunitSetting・記述子dataset用producer・カスタムテンプレート駆動時の出力パス」
 * だけをここで定義する。
 */
enum ScaffoldTarget {
    ddl(GenerateType.ddl) {
        @Override
        Column[] datasetSchema() {
            return ComparableDdlMetaDataProducer.outputSchema();
        }

        @Override
        ComparableDataSetProducerWrapper wrapProducer(final ScaffoldOption option, final ComparableDataSetProducer source) {
            return new ComparableDdlMetaDataProducer(source);
        }

        @Override
        String customTemplateResultPath() {
            return "$param.tableName$.sql";
        }
    },
    javaBean(GenerateType.javaBean) {
        @Override
        Column[] datasetSchema() {
            return ComparableDdlMetaDataProducer.outputSchema();
        }

        @Override
        ComparableDataSetProducerWrapper wrapProducer(final ScaffoldOption option, final ComparableDataSetProducer source) {
            return new ComparableDdlMetaDataProducer(source);
        }

        @Override
        String customTemplateResultPath() {
            return new TemplateRender.Builder().build().getAttributeName("tableName", "snakeToUpperCamel") + ".java";
        }
    },
    xlsxSchema(GenerateType.xlsxSchema) {
        // サンプルunitSettingはScaffold専用リソースで、GenerateType.defaultSettingsPath()には意図的に
        // 接続しない: あちらは素の-generateType=xlsxSchema呼び出し全てに効く全体デフォルトであり、
        // 「PK/CELLSサブテーブルへseparateする」ルールはScaffoldが書き出す記述子datasetに対してのみ意味を持つ
        @Override
        String sampleUnitSettingPath() {
            return "xlsxschema/xlsxSchemaSettings.json";
        }

        @Override
        Column[] datasetSchema() {
            return ComparableXlsxSchemaMetaDataProducer.outputSchema();
        }

        @Override
        ComparableDataSetProducerWrapper wrapProducer(final ScaffoldOption option, final ComparableDataSetProducer source) {
            return new ComparableXlsxSchemaMetaDataProducer(source);
        }

        @Override
        String customTemplateResultPath() {
            return "$param.tableName$.json";
        }
    },
    fixedColumnDef(GenerateType.fixedColumnDef) {
        // 空のプレースホルダー（列の絞り込み・改名を後から足したい利用者向け）
        @Override
        String sampleUnitSettingPath() {
            return "fixedcolumndef/fixedColumnDefSettings.json";
        }

        @Override
        Column[] datasetSchema() {
            return ComparableFixedColumnDefMetaDataProducer.outputSchema();
        }

        @Override
        ComparableDataSetProducerWrapper wrapProducer(final ScaffoldOption option, final ComparableDataSetProducer source) {
            final String[] lengths = Strings.isNotEmpty(option.fixedLength())
                    ? option.fixedLength().split(",") : new String[0];
            return new ComparableFixedColumnDefMetaDataProducer(source, lengths,
                                                                option.defaultLength(), option.align());
        }

        @Override
        String customTemplateResultPath() {
            return "$param.tableName$.json";
        }

        @Override
        void putBuiltinExtraParams(final ScaffoldOption option, final Option.ParametersBuilder builder) {
            builder.put("-fixedLength", option.fixedLength())
                   .put("-defaultLength", Integer.toString(option.defaultLength()))
                   .put("-align", option.align());
        }
    };

    private final GenerateType generateType;

    ScaffoldTarget(final GenerateType generateType) {
        this.generateType = generateType;
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
        return this.generateType.getStgPath();
    }

    String templatePath() {
        return this.generateType.getTemplatePath();
    }

    /** unitSetting雛型としてコピーするclasspathリソース（既定はGenerateTypeのdefaultSettingsPath()） */
    String sampleUnitSettingPath() {
        return this.generateType.defaultSettingsPath();
    }

    /** 記述子datasetの列定義（headerless出力時の-headerName用） */
    abstract Column[] datasetSchema();

    /** 記述子dataset合成用producer。GenerateType.wrapProducer()と同じサブクラスを直接インスタンス化する */
    abstract ComparableDataSetProducerWrapper wrapProducer(ScaffoldOption option, ComparableDataSetProducer source);

    /** カスタムテンプレート（generateType=txt）駆動時の-resultPathパターン */
    abstract String customTemplateResultPath();

    /** 組み込みgenerateType駆動の.paramに追記するtarget固有オプション */
    void putBuiltinExtraParams(final ScaffoldOption option, final Option.ParametersBuilder builder) {
    }
}
