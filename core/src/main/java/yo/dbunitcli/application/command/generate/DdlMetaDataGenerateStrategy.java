package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.producer.ComparableDdlMetaDataProducer;

/**
 * ddl/javaBean共通: ComparableDdlMetaDataProducerの記述子行（DBソースはJDBCメタデータで補完）から生成する
 */
abstract class DdlMetaDataGenerateStrategy implements DescriptorGenerateStrategy {

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
}
