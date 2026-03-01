package yo.dbunitcli.sidecar.controller;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
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
}
