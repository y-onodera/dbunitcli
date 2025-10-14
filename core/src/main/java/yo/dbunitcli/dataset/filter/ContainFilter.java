package yo.dbunitcli.dataset.filter;

import yo.dbunitcli.common.TableMetaDataFilter;

import java.util.List;

/**
 * パターン文字列を含むかを判定するフィルタ
 *
 * @param patternStrings パターン文字列
 */
public record ContainFilter(List<String> patternStrings) implements TableMetaDataFilter {
    @Override
    public boolean test(final String tableName) {
        return this.patternStrings().stream()
                .anyMatch(patternString -> tableName.contains(patternString) || patternString.equals("*"));
    }
}