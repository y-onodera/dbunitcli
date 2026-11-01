package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;

/**
 * 元ソースのメタデータから「列ごとに1行」の記述子行を合成して生成する固定成果物型（ddl/javaBean/xlsxSchema/fixedColumnDef）。
 * 記述子producerはGenerate（wrapProducer）とScaffoldの記述子dataset雛型（ScaffoldTarget）で共有する
 */
public interface DescriptorGenerateStrategy extends GenerateStrategy {

    /** 記述子行の列定義 */
    Column[] descriptorSchema();

    /** 記述子行を合成するproducer */
    ComparableDataSetProducerWrapper wrapDescriptor(DescriptorOption option, ComparableDataSetProducer source);

    /** 記述子producerの合成に使うオプションをパラメータに出す（Generateの固有オプションとScaffoldのbuiltin駆動.paramで共有） */
    default void putDescriptorParameters(final DescriptorOption option, final Option.ParametersBuilder builder) {
    }

    @Override
    default void putOptionParameters(final GenerateContext option, final Option.ParametersBuilder builder) {
        this.putDescriptorParameters(option, builder);
    }

    @Override
    default ComparableDataSetProducer wrapProducer(final GenerateContext option, final ComparableDataSetProducer producer) {
        return this.wrapDescriptor(option, producer);
    }

    @Override
    default ParameterUnit fixedUnit() {
        return ParameterUnit.table;
    }

    @Override
    default boolean loadData() {
        return false;
    }
}
