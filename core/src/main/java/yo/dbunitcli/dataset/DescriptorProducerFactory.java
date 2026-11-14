package yo.dbunitcli.dataset;

/**
 * 読み込んだテーブルを記述子テーブル（列ごとに1行）に置き換えるproducerを作る
 */
@FunctionalInterface
public interface DescriptorProducerFactory {

    ComparableDataSetProducerWrapper wrap(ComparableDataSetProducer source);
}
