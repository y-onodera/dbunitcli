package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.application.command.GenerateOption;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.producer.ComparableFixedColumnDefMetaDataProducer;

/**
 * fixedColumnDef: テーブル毎に固定長ファイルの列定義（json）を生成する固定成果物型
 */
public final class FixedColumnDefGenerateStrategy implements DescriptorGenerateStrategy {

    @Override
    public String stgPath() {
        return "fixedcolumndef/fixedColumnDefTemplate.stg";
    }

    @Override
    public String templatePath() {
        return "fixedcolumndef/fixedColumnDefTemplate.txt";
    }

    @Override
    public Column[] descriptorSchema() {
        return ComparableFixedColumnDefMetaDataProducer.outputSchema();
    }

    @Override
    public ComparableDataSetProducerWrapper wrapDescriptor(final DescriptorOption option, final ComparableDataSetProducer source) {
        return ComparableFixedColumnDefMetaDataProducer.of(source, option.fixedLength(), option.defaultLength(), option.align());
    }

    @Override
    public String resultPathTemplate(final GenerateOption option) {
        return ResultPaths.tableFile(option);
    }
}
