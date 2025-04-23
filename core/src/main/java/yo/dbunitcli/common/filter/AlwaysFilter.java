package yo.dbunitcli.common.filter;

/**
 * 常に同じ結果を返すフィルタ
 *
 * @param result 返す結果
 */
public record AlwaysFilter(boolean result) implements TargetFilter {
    @Override
    public boolean test(final String tableName) {
        return this.result();
    }
}