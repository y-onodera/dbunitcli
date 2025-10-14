package yo.dbunitcli.dataset.filter;

import yo.dbunitcli.common.TableMetaDataFilter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 正規表現パターンによるテーブル名/シート名のフィルタリングを行うクラス
 */
public record RegexFilter(Pattern pattern) implements TableMetaDataFilter {

    /**
     * 指定された正規表現パターンでフィルタを作成
     *
     * @param regex 正規表現パターン
     * @throws PatternSyntaxException 正規表現パターンが不正な場合
     * @throws NullPointerException   引数がnullの場合
     */
    public RegexFilter(final String regex) {
        this(Pattern.compile(regex));
    }

    @Override
    public boolean test(final String tableName) {
        if (tableName == null) {
            return false;
        }
        return this.pattern().matcher(tableName).matches();
    }
}