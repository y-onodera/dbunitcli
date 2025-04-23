package yo.dbunitcli.common.filter;

/**
 * パターン文字列を含むかを判定するフィルタ
 *
 * @param patternString パターン文字列
 */
public record ContainFilter(String patternString) implements TargetFilter {
    @Override
    public boolean test(final String tableName) {
        return tableName.contains(this.patternString()) || this.patternString().equals("*");
    }
}