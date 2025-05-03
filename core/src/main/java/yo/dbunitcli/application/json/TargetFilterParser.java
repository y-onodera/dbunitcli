package yo.dbunitcli.application.json;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import yo.dbunitcli.common.filter.TargetFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * JSONオブジェクトからTargetFilterインスタンスを生成するパーサー
 */
public class TargetFilterParser {
    private final JsonObject jsonObject;
    private final String key;

    /**
     * コンストラクタ
     *
     * @param jsonObject パース対象のJSONオブジェクト
     * @throws IllegalArgumentException jsonObjectがnullの場合
     */
    public TargetFilterParser(final JsonObject jsonObject, final String key) {
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
    public TargetFilter parseEquals() {
        if (this.jsonObject.isEmpty()) {
            return TargetFilter.always(true);
        }

        final JsonValue value = this.jsonObject.get(this.key);
        final TargetFilter filter;
        switch (value.getValueType()) {
            case STRING -> filter = TargetFilter.any(((JsonString) value).getString());
            case ARRAY -> filter = TargetFilter.any(this.parseArrayValue(value.asJsonArray()));
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
    public TargetFilter parsePattern() {
        if (this.jsonObject.isEmpty()) {
            return TargetFilter.always(true);
        }

        final JsonValue value = this.jsonObject.get(this.key);
        if (value == null) {
            return TargetFilter.always(true);
        }

        final TargetFilter filter;
        switch (value.getValueType()) {
            case STRING -> filter = TargetFilter.contain(((JsonString) value).getString());
            case ARRAY -> filter = TargetFilter.contain(this.parseArrayValue(value.asJsonArray()));
            case OBJECT -> filter = this.parseObjectValue(value.asJsonObject());
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

    /**
     * オブジェクトからTargetFilterを生成
     *
     * @param obj JSONオブジェクト
     * @return TargetFilterインスタンス
     */
    private TargetFilter parseObjectValue(final JsonObject obj) {
        final TargetFilter filter = this.parseBaseFilter(obj);
        if (obj.containsKey("exclude")) {
            final JsonArray excludes = obj.getJsonArray("exclude");
            return filter.exclude(this.parseExcludeList(excludes));
        }
        return filter;
    }

    private TargetFilter parseBaseFilter(final JsonObject obj) {
        if (obj.containsKey("regex")) {
            return TargetFilter.regex(obj.getString("regex"));
        } else if (obj.containsKey("string")) {
            return TargetFilter.contain(obj.getString("string"));
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