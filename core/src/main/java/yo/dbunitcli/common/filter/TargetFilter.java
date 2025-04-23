package yo.dbunitcli.common.filter;

import java.util.List;

/**
 * テーブル名やシート名に対するフィルタリング機能を提供するインターフェース。
 */
public interface TargetFilter {

    /**
     * 指定された名前のリストに完全一致するかを判定するフィルタを作成
     *
     * @param names 対象とする名前の配列
     * @return フィルタのインスタンス
     */
    static TargetFilter any(final String... names) {
        return new AnyFilter(List.of(names));
    }

    /**
     * パターン文字列を含むかを判定するフィルタを作成
     *
     * @param pattern パターン文字列
     * @return フィルタのインスタンス
     */
    static TargetFilter contain(final String pattern) {
        return new ContainFilter(pattern);
    }

    /**
     * 指定された名前のリストを除外するフィルタを作成
     *
     * @param names 除外する名前のリスト
     * @return フィルタのインスタンス
     */
    default TargetFilter exclude(final List<String> names) {
        return new ExcludeFilter(this, names);
    }

    /**
     * 常に同じ結果を返すフィルタを作成
     *
     * @param result 返す結果
     * @return フィルタのインスタンス
     */
    static TargetFilter always(final boolean result) {
        return new AlwaysFilter(result);
    }

    /**
     * フィルタの判定を実行
     *
     * @param tableName テーブル名
     * @return フィルタ条件に一致する場合true
     */
    boolean test(String tableName);
}