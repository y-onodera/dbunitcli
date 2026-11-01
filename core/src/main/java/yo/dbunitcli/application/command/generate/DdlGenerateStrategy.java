package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.producer.ComparableDdlMetaDataProducer;

/**
 * ddl: テーブル毎にCREATE TABLE文を生成する固定成果物型
 */
public final class DdlGenerateStrategy implements DescriptorGenerateStrategy {

    @Override
    public String stgPath() {
        return "sql/ddlTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "sql/ddlTemplate.txt";
    }

    @Override
    public String defaultSettingsPath() {
        return "sql/ddlSettings.json";
    }

    @Override
    public boolean useJdbcMetaData() {
        return true;
    }

    @Override
    public Column[] descriptorSchema() {
        return ComparableDdlMetaDataProducer.outputSchema();
    }

    @Override
    public ComparableDataSetProducerWrapper wrapDescriptor(final DescriptorOption option, final ComparableDataSetProducer source) {
        return ComparableDdlMetaDataProducer.forSource(source);
    }

    @Override
    public String resultPathTemplate(final GenerateOption option) {
        return ResultPaths.sql(option);
    }
}
