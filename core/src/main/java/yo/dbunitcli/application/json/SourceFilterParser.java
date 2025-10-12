package yo.dbunitcli.application.json;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import yo.dbunitcli.dataset.SourceFilter;
import yo.dbunitcli.dataset.filter.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

/**
 * JSONオブジェクトからTargetFilterインスタンスを生成するパーサー
 */
public class SourceFilterParser {
    private final JsonObject jsonObject;
    private final String key;

    public static SourceFilter withFilePathMatch(final SourceFilter base, final String filePath) {
        return new WithFilePathMatchFilter(base, filePath);
    }

    /**
     * 正規表現パターンでマッチングするフィルタを作成
     *
     * @param regex 正規表現パターン
     * @return フィルタのインスタンス
     * @throws PatternSyntaxException 不正な正規表現パターンの場合
     * @throws NullPointerException   パターンがnullの場合
     */
    public static SourceFilter regex(final String regex) {
        return new RegexFilter(regex);
    }

    /**
     * 指定された名前のリストに完全一致するかを判定するフィルタを作成
     *
     * @param names 対象とする名前の配列
     * @return フィルタのインスタンス
     */
    public static SourceFilter any(final String... names) {
        return new AnyFilter(List.of(names));
    }

    /**
     * パターン文字列を含むかを判定するフィルタを作成
     *
     * @param pattern パターン文字列
     * @return フィルタのインスタンス
     */
    public static SourceFilter contain(final String... pattern) {
        return new ContainFilter(List.of(pattern));
    }

    /**
     * 常に同じ結果を返すフィルタを作成
     *
     * @param result 返す結果
     * @return フィルタのインスタンス
     */
    public static SourceFilter always(final boolean result) {
        return new AlwaysFilter(result);
    }

    /**
     * コンストラクタ
     *
     * @param jsonObject パース対象のJSONオブジェクト
     * @throws IllegalArgumentException jsonObjectがnullの場合
     */
    public SourceFilterParser(final JsonObject jsonObject, final String key) {
        this.key = key;
        if (jsonObject == null) {
            throw new IllegalArgumentException("jsonObject must not be null");
        }
        this.jsonObject = jsonObject;
    }

    /**
     * 指定されたキーを使用してJSONオブジェクトからTargetFilterを生成
     *
     * @return 生成されたTargetFilterインスタンス
     * @throws IllegalArgumentException JSONの構造が不正な場合やキーが存在しない場合
     */
    public SourceFilter parseEquals() {
        if (this.jsonObject.isEmpty()) {
            return always(true);
        }

        final JsonValue value = this.jsonObject.get(this.key);
        final SourceFilter filter;
        switch (value.getValueType()) {
            case STRING -> filter = any(((JsonString) value).getString());
            case ARRAY -> filter = any(this.parseArrayValue(value.asJsonArray()));
            case OBJECT -> filter = this.parseFilePathFilter(value.asJsonObject(), this::any);
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getValueType());
        }

        return filter;
    }

    /**
     * 指定されたキーを使用してJSONオブジェクトからTargetFilterを生成
     *
     * @return 生成されたTargetFilterインスタンス
     * @throws IllegalArgumentException JSONの構造が不正な場合やキーが存在しない場合
     */
    public SourceFilter parsePattern() {
        if (this.jsonObject.isEmpty()) {
            return always(true);
        }

        final JsonValue value = this.jsonObject.get(this.key);
        if (value == null) {
            return always(true);
        }

        final SourceFilter filter;
        switch (value.getValueType()) {
            case STRING -> filter = contain(((JsonString) value).getString());
            case ARRAY -> filter = contain(this.parseArrayValue(value.asJsonArray()));
            case OBJECT -> filter = this.parseFilePathFilter(value.asJsonObject(), this::contain);
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getValueType());
        }

        return filter;
    }

    /**
     * JSON配列から文字列配列を生成
     *
     * @param array JSON配列
     * @return 文字列配列
     * @throws IllegalArgumentException 配列に文字列以外の要素が含まれている場合
     */
    private String[] parseArrayValue(final JsonArray array) {
        final List<String> values = new ArrayList<>();
        for (final JsonValue item : array) {
            if (item.getValueType() != JsonValue.ValueType.STRING) {
                throw new IllegalArgumentException("Array must contain only strings");
            }
            values.add(((JsonString) item).getString());
        }
        return values.toArray(new String[0]);
    }

    private SourceFilter parseFilePathFilter(final JsonObject obj, final Function<JsonObject, SourceFilter> baseParse) {
        final SourceFilter result = baseParse.apply(obj);
        if (obj.containsKey("filePath")) {
            return withFilePathMatch(result, obj.getString("filePath"));
        }
        return result;
    }

    private SourceFilter any(final JsonObject obj) {
        final JsonValue value = obj.get("any");
        return switch (value.getValueType()) {
            case STRING -> any(((JsonString) value).getString());
            case ARRAY -> any(this.parseArrayValue(value.asJsonArray()));
            default -> throw new IllegalArgumentException("Unsupported value type: " + value.getValueType());

        };
    }

    /**
     * オブジェクトからTargetFilterを生成
     *
     * @param obj JSONオブジェクト
     * @return TargetFilterインスタンス
     */
    private SourceFilter contain(final JsonObject obj) {
        final SourceFilter filter = this.parseContainFilter(obj);
        if (obj.containsKey("exclude")) {
            final JsonArray excludes = obj.getJsonArray("exclude");
            return filter.exclude(this.parseExcludeList(excludes));
        }
        return filter;
    }

    private SourceFilter parseContainFilter(final JsonObject obj) {
        if (obj.containsKey("regex")) {
            return regex(obj.getString("regex"));
        } else if (obj.containsKey("string")) {
            return contain(obj.getString("string"));
        } else {
            throw new IllegalArgumentException("Object must contain either 'regex' or 'string' property");
        }
    }

    /**
     * 除外リストを解析
     *
     * @param excludes JSON配列
     * @return 除外する名前のリスト
     */
    private List<String> parseExcludeList(final JsonArray excludes) {
        final List<String> result = new ArrayList<>();
        for (final JsonValue item : excludes) {
            if (item.getValueType() != JsonValue.ValueType.STRING) {
                throw new IllegalArgumentException("Excludes must contain only strings");
            }
            result.add(((JsonString) item).getString());
        }
        return result;
    }
}