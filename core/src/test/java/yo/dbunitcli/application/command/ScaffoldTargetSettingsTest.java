package yo.dbunitcli.application.command;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import yo.dbunitcli.resource.FileResources;

import java.io.StringReader;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * サンプルunitSettingは利用者が他のデータソースの列をテンプレートの読む列へマッピングする際の
 * ヒントであるため、テンプレートが参照する全列がstring/boolean等の式として定義されている必要がある。
 * テンプレート側へ列参照が追加された際にサンプルとのドリフトを検出する。
 */
public class ScaffoldTargetSettingsTest {

    /** 行変数経由の列参照（$row.X$ / $r.X$ / $col.X$ / $first(...rows).X$） */
    private static final Pattern ROW_ATTRIBUTE =
            Pattern.compile("\\b(?:row|r|col)\\.(\\w+)|first\\([^)]*\\)\\.(\\w+)");
    /** 列名ではない行変数の属性（unit=tableのテーブル構造アクセス） */
    private static final Set<String> NON_COLUMN_ATTRIBUTES = Set.of("rows", "tableName", "dataset", "values");
    private static final Set<String> EXPRESSION_KEYS = Set.of("string", "boolean", "number", "sqlFunction");

    @ParameterizedTest
    @EnumSource(ScaffoldTarget.class)
    public void testSampleUnitSettingDefinesAllTemplateColumns(final ScaffoldTarget target) {
        final Set<String> templateColumns = new TreeSet<>();
        this.collectTemplateColumns(FileResources.readClasspathResource(target.stgPath()), templateColumns);
        this.collectTemplateColumns(FileResources.readClasspathResource(target.templatePath()), templateColumns);
        final Set<String> definedColumns = new TreeSet<>();
        try (final JsonReader reader = Json.createReader(
                new StringReader(FileResources.readClasspathResource(target.sampleUnitSettingPath())))) {
            this.collectDefinedColumns(reader.readObject(), definedColumns);
        }
        final Set<String> missing = new TreeSet<>(templateColumns);
        missing.removeAll(definedColumns);
        assertTrue(missing.isEmpty(),
                () -> target + ": " + target.sampleUnitSettingPath() + " はテンプレートが参照する列 "
                        + missing + " を定義していない");
    }

    private void collectTemplateColumns(final String template, final Set<String> result) {
        final Matcher matcher = ROW_ATTRIBUTE.matcher(template);
        while (matcher.find()) {
            final String attribute = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (!NON_COLUMN_ATTRIBUTES.contains(attribute)) {
                result.add(attribute);
            }
        }
    }

    private void collectDefinedColumns(final JsonValue json, final Set<String> result) {
        switch (json.getValueType()) {
            case OBJECT -> json.asJsonObject().forEach((key, value) -> {
                if (EXPRESSION_KEYS.contains(key) && value.getValueType() == JsonValue.ValueType.OBJECT) {
                    result.addAll(value.asJsonObject().keySet());
                } else {
                    this.collectDefinedColumns(value, result);
                }
            });
            case ARRAY -> json.asJsonArray().forEach(it -> this.collectDefinedColumns(it, result));
            default -> {
            }
        }
    }
}
