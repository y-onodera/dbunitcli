package yo.dbunitcli.common.filter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 正規表現パターンによるテーブル名/シート名のフィルタリングを行うクラス
 */
public class RegexFilter implements TargetFilter {
    private final Pattern pattern;

    /**
     * 指定された正規表現パターンでフィルタを作成
     *
     * @param regex 正規表現パターン
     * @throws PatternSyntaxException 正規表現パターンが不正な場合
     * @throws IllegalArgumentException 引数がnullの場合
     */
    public RegexFilter(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("regex must not be null");
        }
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean test(String tableName) {
        if (tableName == null) {
            return false;
        }
        return pattern.matcher(tableName).matches();
    }
}