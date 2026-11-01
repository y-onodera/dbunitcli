package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.producer.ComparableXlsxSchemaMetaDataProducer;

/**
 * xlsxSchema: テーブル毎にxlsxの読込スキーマ（json）を生成する固定成果物型
 */
public final class XlsxSchemaGenerateStrategy implements DescriptorGenerateStrategy {

    @Override
    public String stgPath() {
        return "xlsxschema/xlsxSchemaTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "xlsxschema/xlsxSchemaTemplate.txt";
    }

    // separateルールでPK/CELLSサブテーブルを合成する（Scaffoldがコピーする雛型と同一ファイル。
    // テンプレートのsubTable.PK/subTable.CELLS参照はこの分割が前提）
    @Override
    public String defaultSettingsPath() {
        return "xlsxschema/xlsxSchemaSettings.json";
    }

    @Override
    public Column[] descriptorSchema() {
        return ComparableXlsxSchemaMetaDataProducer.outputSchema();
    }

    @Override
    public ComparableDataSetProducerWrapper wrapDescriptor(final DescriptorOption option, final ComparableDataSetProducer source) {
        return new ComparableXlsxSchemaMetaDataProducer(source);
    }

    @Override
    public String resultPathTemplate(final GenerateOption option) {
        return ResultPaths.tableFile(option);
    }
}
