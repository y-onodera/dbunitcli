package yo.dbunitcli.sidecar.controller;

import org.junit.jupiter.api.Assertions;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

class JsonTestHelper {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    static String normalize(final String json) {
        return WHITESPACE.matcher(json).replaceAll("");
    }

    static void assertJsonEquals(final String expected, final String actual) {
        Assertions.assertEquals(normalize(expected), normalize(actual));
    }

    static void assertJsonEquals(final Path expectedFile, final String actual) throws IOException {
        assertJsonEquals(Files.readString(expectedFile), actual);
    }

    static void assertJsonEquals(final Path expectedFile, final String actual, final String... args) throws IOException {
        assertJsonEquals(String.format(Files.readString(expectedFile), (Object[]) args), actual);
    }

    /**
     * リソースのloadの応答（{"content": ..., "location": ...}）のcontentをJSONの文字列にする
     */
    static String contentOf(final String loadResponse) {
        return loadResponseOf(loadResponse).get("content").toString();
    }

    static JsonObject loadResponseOf(final String loadResponse) {
        try (final JsonReader reader = Json.createReader(new StringReader(loadResponse))) {
            return reader.readObject();
        }
    }
}
