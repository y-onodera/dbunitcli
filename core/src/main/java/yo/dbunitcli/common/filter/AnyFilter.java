package yo.dbunitcli.common.filter;

import java.util.List;

/**
 * 指定された名前のリストに完全一致するかを判定するフィルタ
 *
 * @param names 対象とする名前のリスト
 */
public record AnyFilter(List<String> names) implements SourceFilter {
    @Override
    public boolean test(final String tableName) {
        return this.names().contains(tableName);
    }
}