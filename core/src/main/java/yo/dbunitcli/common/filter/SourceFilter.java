package yo.dbunitcli.common.filter;

import org.dbunit.dataset.ITableMetaData;

import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * テーブル名やシート名に対するフィルタリング機能を提供するインターフェース。
 */
public interface SourceFilter {

    /**
     * 正規表現パターンでマッチングするフィルタを作成
     *
     * @param regex 正規表現パターン
     * @return フィルタのインスタンス
     * @throws PatternSyntaxException 不正な正規表現パターンの場合
     * @throws NullPointerException   パターンがnullの場合
     */
    static SourceFilter regex(final String regex) {
        return new RegexFilter(regex);
    }

    /**
     * 指定された名前のリストに完全一致するかを判定するフィルタを作成
     *
     * @param names 対象とする名前の配列
     * @return フィルタのインスタンス
     */
    static SourceFilter any(final String... names) {
        return new AnyFilter(List.of(names));
    }

    /**
     * パターン文字列を含むかを判定するフィルタを作成
     *
     * @param pattern パターン文字列
     * @return フィルタのインスタンス
     */
    static SourceFilter contain(final String... pattern) {
        return new ContainFilter(List.of(pattern));
    }

    /**
     * 常に同じ結果を返すフィルタを作成
     *
     * @param result 返す結果
     * @return フィルタのインスタンス
     */
    static SourceFilter always(final boolean result) {
        return new AlwaysFilter(result);
    }

    static SourceFilter withFilePathMatch(final SourceFilter base, final String filePath) {
        return new WithFilePathMatchFilter(base, filePath);
    }

    /**
     * 指定された名前のリストを除外するフィルタを作成
     *
     * @param names 除外する名前のリスト
     * @return フィルタのインスタンス
     */
    default SourceFilter exclude(final List<String> names) {
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
     * フィルタの判定を実行
     *
     * @param tableName テーブル名
     * @return フィルタ条件に一致する場合true
     */
    boolean test(String tableName);
}