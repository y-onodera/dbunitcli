package yo.dbunitcli.application.command.generate;

import org.dbunit.dataset.Column;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;
import yo.dbunitcli.dataset.DescriptorProducerFactory;
import yo.dbunitcli.resource.st4.TemplateRender;

/**
 * 元ソースのメタデータから「列ごとに1行」の記述子行を合成して生成する固定成果物型（ddl/javaBean/xlsxSchema/fixedColumnDef）。
 * 記述子producerはGenerateとScaffoldの記述子dataset雛型（ScaffoldTarget）で共有する
 */
public interface DescriptorGenerateStrategy extends GenerateStrategy {

    /** 記述子行の列定義 */
    Column[] descriptorSchema();

    /** 記述子行を合成するproducer */
    ComparableDataSetProducerWrapper wrapDescriptor(DescriptorOption option, ComparableDataSetProducer source);

    /** テーブル毎の出力ファイルの拡張子 */
    String extension();

    /** 出力ファイル名のtableNameに適用するformat。なければnull */
    default String tableNameFormat() {
        return null;
    }

    /**
     * テーブル毎の出力ファイル名のパターン。Generateの-resultPath配下のファイル名と、
     * Scaffoldのtxt駆動.paramの-resultPathで共有する（両駆動の出力ファイル名を一致させるため）
     */
    default String tableFileName(final TemplateRender render) {
        return ResultPaths.fileName(render, "", "", this.extension(), this.tableNameFormat());
    }

    @Override
    default String resultPathTemplate(final GenerateContext option) {
        return ResultPaths.tableFile(option, "", "", this.extension(), this.tableNameFormat());
    }

    /** 記述子producerの合成に使うオプションをパラメータに出す（Generateの固有オプションとScaffoldのbuiltin駆動.paramで共有） */
    default void putDescriptorParameters(final DescriptorOption option, final Option.ParametersBuilder builder) {
    }

    @Override
    default void putOptionParameters(final GenerateContext option, final Option.ParametersBuilder builder) {
        this.putDescriptorParameters(option, builder);
    }

    @Override
    default DescriptorProducerFactory descriptor(final GenerateContext option) {
        return source -> this.wrapDescriptor(option, source);
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
