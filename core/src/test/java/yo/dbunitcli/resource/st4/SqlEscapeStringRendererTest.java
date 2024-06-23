package yo.dbunitcli.resource.st4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlEscapeStringRendererTest {
    private final SqlEscapeStringRenderer target = new SqlEscapeStringRenderer();
    private String baseDir;

    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        this.baseDir = URLDecoder.decode(Objects.requireNonNull(this.getClass().getResource(".")).getPath(), StandardCharsets.UTF_8)
                .replace("target/test-classes", "src/test/resources");
    }

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
        assertEquals("'te' || CHR(13) || CHR(10) || 'st'", this.target.toString((Object) "te\r\nst", "escapeSql", Locale.JAPANESE));
    }

    @Test
    void testToStringEscapeSqlToClob4000ByteOver() throws IOException {
        final Object toStringTarget = this.readString("4000byteOver.txt");
        assertEquals(this.readString("4000byteOverQuoted.txt"), this.target.toString(toStringTarget, "escapeSql", Locale.JAPANESE));
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

    private String readString(final String fileName) throws IOException {
        return Files.readString(new File(this.baseDir, fileName).toPath(), StandardCharsets.UTF_8);
    }
}