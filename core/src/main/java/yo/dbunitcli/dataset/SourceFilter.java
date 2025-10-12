package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;

import java.util.List;

/**
 * テーブル名やシート名に対するフィルタリング機能を提供するインターフェース。
 */
public interface SourceFilter {

    /**
     * 指定された名前のリストを除外するフィルタを作成
     *
     * @param names 除外する名前のリスト
     * @return フィルタのインスタンス
     */
    default SourceFilter exclude(final List<String> names) {
        return new ExcludeFilter(this, names);
    }

    /**
     * フィルタの判定を実行
     *
     * @return フィルタ条件に一致する場合true
     */
    default boolean test(final ITableMetaData originMetaData) {
        return this.test(originMetaData.getTableName());
    }

    /**
     * フィルタの判定を実行
     *
     * @param tableName テーブル名
     * @return フィルタ条件に一致する場合true
     */
    boolean test(String tableName);

    /**
     * 指定された名前のリストを除外するフィルタ
     *
     * @param base  基本となるフィルタ
     * @param names 除外する名前のリスト
     */
    record ExcludeFilter(SourceFilter base, List<String> names) implements SourceFilter {
        @Override
        public boolean test(final String tableName) {
            return this.base().test(tableName) && !this.names().contains(tableName);
        }
    }
}