package yo.dbunitcli.dataset.filter;

import yo.dbunitcli.common.TableMetaDataFilter;

/**
 * 常に同じ結果を返すフィルタ
 *
 * @param result 返す結果
 */
public record AlwaysFilter(boolean result) implements TableMetaDataFilter {
    @Override
    public boolean test(final String tableName) {
        return this.result();
    }
}