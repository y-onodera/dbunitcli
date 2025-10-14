package yo.dbunitcli.common;

import org.dbunit.dataset.ITableMetaData;

import java.util.List;

/**
 * テーブル名やシート名に対するフィルタリング機能を提供するインターフェース。
 */
public interface TableMetaDataFilter extends yo.dbunitcli.common.TargetFilter {

    /**
     * 指定された名前のリストを除外するフィルタを作成
     *
     * @param names 除外する名前のリスト
     * @return フィルタのインスタンス
     */
    @Override
    default TableMetaDataFilter exclude(final List<String> names) {
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
     * 指定された名前のリストを除外するフィルタ
     *
     * @param base  基本となるフィルタ
     * @param names 除外する名前のリスト
     */
    record ExcludeFilter(TableMetaDataFilter base, List<String> names) implements TableMetaDataFilter {
        @Override
        public boolean test(final String tableName) {
            return this.base().test(tableName) && !this.names().contains(tableName);
        }
    }

}