package yo.dbunitcli.common.filter;

import java.util.List;

/**
 * 指定された名前のリストを除外するフィルタ
 *
 * @param base 基本となるフィルタ
 * @param names 除外する名前のリスト
 */
public record ExcludeFilter(TargetFilter base, List<String> names) implements TargetFilter {
    @Override
    public boolean test(final String tableName) {
        return this.base().test(tableName) && !this.names().contains(tableName);
    }
}