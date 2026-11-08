package yo.dbunitcli.resource.st4;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlEscapeStringRendererTest {
    private final SqlEscapeStringRenderer target = new SqlEscapeStringRenderer();

    @Test
    void testToStringJexlExp() {
        assertEquals("${test}", this.target.toString((Object) "test", "jexlExp", Locale.JAPANESE));
    }

    @Test
    void testToStringST4Exp() {
        assertEquals("$test$", this.target.toString((Object) "test", "ST4Exp", Locale.JAPANESE));
    }

    @Test
    void testToStringEscapeSql() {
        assertEquals("'test'", this.target.toString((Object) "test", "escapeSql", Locale.JAPANESE));
    }

    @Test
    void testToStringEscapeSqlSingleQuoteEscape() {
        assertEquals("'''test'''", this.target.toString((Object) "'test'", "escapeSql", Locale.JAPANESE));
    }

    @Test
    void testToStringEscapeSqLineSeparatorEscape() {
        assertEquals("'te' || CHR(10) || 'st'", this.target.toString((Object) "te\nst", "escapeSql", Locale.JAPANESE));
        assertEquals("'te' || CHR(13) || CHR(10) || 'st'",
                     this.target.toString((Object) "te\r\nst", "escapeSql", Locale.JAPANESE));
    }

    @Test
    void testToStringCamelToSnake() {
        assertEquals("camel_case", this.target.toString((Object) "camelCase", "camelToSnake", Locale.JAPANESE));
        assertEquals("camelcase", this.target.toString((Object) "camelcase", "camelToSnake", Locale.JAPANESE));
    }

    @Test
    void testToStringCamelToKebab() {
        assertEquals("camel-case", this.target.toString((Object) "camelCase", "camelToKebab", Locale.JAPANESE));
        assertEquals("camelcase", this.target.toString((Object) "camelcase", "camelToKebab", Locale.JAPANESE));
    }

    @Test
    void testToStringCamelToUpperSnake() {
        assertEquals("CAMEL_CASE", this.target.toString((Object) "camelCase", "camelToUpperSnake", Locale.JAPANESE));
        assertEquals("CAMELCASE", this.target.toString((Object) "camelcase", "camelToUpperSnake", Locale.JAPANESE));
    }

    @Test
    void testToStringCamelToUpperKebab() {
        assertEquals("CAMEL-CASE", this.target.toString((Object) "camelCase", "camelToUpperKebab", Locale.JAPANESE));
        assertEquals("CAMELCASE", this.target.toString((Object) "camelcase", "camelToUpperKebab", Locale.JAPANESE));
    }

    @Test
    void testToStringUpperSnakeToCamel() {
        assertEquals("camelCase", this.target.toString((Object) "CAMEL_CASE", "snakeToCamel", Locale.JAPANESE));
        assertEquals("camelCase", this.target.toString((Object) "camel_case", "snakeToCamel", Locale.JAPANESE));
        assertEquals("camelcase", this.target.toString((Object) "camelcase", "snakeToCamel", Locale.JAPANESE));
        assertEquals("camelcase", this.target.toString((Object) "CAMELCASE", "snakeToCamel", Locale.JAPANESE));
    }

    @Test
    void testToStringUpperKebabToCamel() {
        assertEquals("camelCase", this.target.toString((Object) "CAMEL-CASE", "kebabToCamel", Locale.JAPANESE));
        assertEquals("camelCase", this.target.toString((Object) "camel-case", "kebabToCamel", Locale.JAPANESE));
        assertEquals("camelcase", this.target.toString((Object) "camelcase", "kebabToCamel", Locale.JAPANESE));
        assertEquals("camelcase", this.target.toString((Object) "CAMELCASE", "kebabToCamel", Locale.JAPANESE));
    }

    @Test
    void testToStringUpperSnakeToUpperCamel() {
        assertEquals("CamelCase", this.target.toString((Object) "CAMEL_CASE", "snakeToUpperCamel", Locale.JAPANESE));
        assertEquals("CamelCase", this.target.toString((Object) "camel_case", "snakeToUpperCamel", Locale.JAPANESE));
        assertEquals("Camelcase", this.target.toString((Object) "camelcase", "snakeToUpperCamel", Locale.JAPANESE));
        assertEquals("Camelcase", this.target.toString((Object) "CAMELCASE", "snakeToUpperCamel", Locale.JAPANESE));
    }

    @Test
    void testToStringUpperKebabToUpperCamel() {
        assertEquals("CamelCase", this.target.toString((Object) "CAMEL-CASE", "kebabToUpperCamel", Locale.JAPANESE));
        assertEquals("CamelCase", this.target.toString((Object) "camel-case", "kebabToUpperCamel", Locale.JAPANESE));
        assertEquals("Camelcase", this.target.toString((Object) "camelcase", "kebabToUpperCamel", Locale.JAPANESE));
        assertEquals("Camelcase", this.target.toString((Object) "CAMELCASE", "kebabToUpperCamel", Locale.JAPANESE));
    }

    @Test
    void testToStringEscapeSqlSplitsMultibyteClobByBytes() {
        final String value = "あ".repeat(2000);
        final String actual = this.target.toString((Object) value, "escapeSql", Locale.JAPANESE);
        // 6000バイトを4000バイト以下（あ=3バイトなので1333文字）ずつに分ける
        assertEquals("TO_CLOB('" + "あ".repeat(1333) + "') || TO_CLOB('" + "あ".repeat(667) + "')", actual);
    }

    @Test
    void testToStringEscapeSqlDoesNotSplitEscapedQuote() {
        final String value = "a".repeat(3999) + "'" + "b";
        final String actual = this.target.toString((Object) value, "escapeSql", Locale.JAPANESE);
        assertEquals("TO_CLOB('" + "a".repeat(3999) + "') || TO_CLOB('''b')", actual);
    }
}
