package yo.dbunitcli.application.json;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.SourceFilter;

import static org.junit.jupiter.api.Assertions.*;

class SourceFilterParserTest {

    @Test
    void パース_空のJSONの場合_常にtrueのフィルタを返す() {
        final JsonObject json = Json.createObjectBuilder().build();
        final SourceFilterParser parser = new SourceFilterParser(json, "pattern");
        final SourceFilter filter = parser.parsePattern();

        assertTrue(filter.test("any_table"));
    }

    @Test
    void パース_parsePatternがない場合_常にTrueのフィルタを返す() {
        final JsonObject json = Json.createObjectBuilder()
                .add("other", "value")
                .build();
        final SourceFilterParser parser = new SourceFilterParser(json, "pattern");
        final SourceFilter filter = parser.parsePattern();

        assertTrue(filter.test("any_table"));
    }

    @Test
    void パース_文字列パターン_containフィルタを生成() {
        final JsonObject json = Json.createObjectBuilder()
                .add("pattern", "test")
                .build();
        final SourceFilterParser parser = new SourceFilterParser(json, "pattern");
        final SourceFilter filter = parser.parsePattern();

        assertTrue(filter.test("test_table"));
        assertFalse(filter.test("other_table"));
    }

    @Test
    void パース_正規表現パターン_regexフィルタを生成() {
        final JsonObject json = Json.createObjectBuilder()
                .add("pattern", Json.createObjectBuilder()
                        .add("regex", "test.*")
                        .build())
                .build();
        final SourceFilterParser parser = new SourceFilterParser(json, "pattern");
        final SourceFilter filter = parser.parsePattern();

        assertTrue(filter.test("test_table"));
        assertTrue(filter.test("testing"));
        assertFalse(filter.test("other_table"));
    }

    @Test
    void パース_文字列パターンとExclude_フィルタとexcludeを生成() {
        final JsonObject json = Json.createObjectBuilder()
                .add("pattern", Json.createObjectBuilder()
                        .add("string", "test")
                        .add("exclude", Json.createArrayBuilder()
                                .add("test_exclude")
                                .build())
                        .build())
                .build();

        final SourceFilterParser parser = new SourceFilterParser(json, "pattern");
        final SourceFilter filter = parser.parsePattern();

        assertTrue(filter.test("test_table"));
        assertFalse(filter.test("test_exclude"));
        assertFalse(filter.test("other_table"));
    }

    @Test
    void コンストラクタ_nullの場合_例外をスロー() {
        assertThrows(IllegalArgumentException.class, () -> new SourceFilterParser(null, null));
    }

}