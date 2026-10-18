package yo.dbunitcli.application.json;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import yo.dbunitcli.common.TableMetaDataFilter;

import java.util.Optional;

/**
 * 設定ファイル最上位の"outputFilter"から、separate/join後に出力するテーブルを絞るフィルタを生成するパーサー。
 * <p>"name"(完全一致)と"pattern"(部分一致/regex)は、settings配下と同じ指定方法を{@link TableMetaDataFilterParser}に委譲する。</p>
 */
public record OutputFilterParser(JsonObject setting) {

    public static final String KEY = "outputFilter";

    public Optional<TableMetaDataFilter> parse() {
        if (!this.setting.containsKey(KEY)) {
            return Optional.empty();
        }
        final JsonValue value = this.setting.get(KEY);
        if (value.getValueType() != JsonValue.ValueType.OBJECT) {
            throw new IllegalArgumentException(KEY + " must be an object with 'name' or 'pattern'");
        }
        final JsonObject json = value.asJsonObject();
        if (json.containsKey("name")) {
            return Optional.of(this.rejectFilePath(json, "name").parseEquals());
        } else if (json.containsKey("pattern")) {
            return Optional.of(this.rejectFilePath(json, "pattern").parsePattern());
        }
        throw new IllegalArgumentException(KEY + " must contain either 'name' or 'pattern'");
    }

    private TableMetaDataFilterParser rejectFilePath(final JsonObject json, final String key) {
        final JsonValue value = json.get(key);
        if (value.getValueType() == JsonValue.ValueType.OBJECT && value.asJsonObject().containsKey("filePath")) {
            throw new IllegalArgumentException(KEY + " does not support 'filePath' (applied to output tables)");
        }
        return new TableMetaDataFilterParser(json, key);
    }
}
