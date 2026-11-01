package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.producer.ComparableDdlMetaDataProducer;

/**
 * javaBean: テーブル毎にJavaBeanクラスを生成する固定成果物型
 */
public final class JavaBeanGenerateStrategy implements DescriptorGenerateStrategy {

    @Override
    public String stgPath() {
        return "javabean/javaBeanTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "javabean/javaBeanTemplate.txt";
    }

    @Override
    public String defaultSettingsPath() {
        return "javabean/javaBeanSettings.json";
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
    public String resultPathTemplate(final GenerateContext option) {
        return ResultPaths.tableFile(option, "", "", ".java", "snakeToUpperCamel");
    }
}
