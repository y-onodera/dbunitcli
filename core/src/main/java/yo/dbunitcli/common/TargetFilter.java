package yo.dbunitcli.common;

import java.util.List;

public interface TargetFilter {

    default TargetFilter exclude(final List<String> names) {
        return new ExcludeFilter(this, names);
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
    record ExcludeFilter(TargetFilter base, List<String> names) implements TargetFilter {
        @Override
        public boolean test(final String tableName) {
            return this.base().test(tableName) && !this.names().contains(tableName);
        }
    }
}
