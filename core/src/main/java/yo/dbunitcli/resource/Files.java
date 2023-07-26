package yo.dbunitcli.resource;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public enum Files {
    SINGLETON;


    public static String readClasspathResource(final String aURL) {
        return readClasspathResource(aURL, StandardCharsets.UTF_8);
    }

    public static String readClasspathResource(final String aURL, final Charset aCharset) {
        try (final InputStream inputStream = Files.class.getClassLoader().getResourceAsStream(aURL)) {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), aCharset))
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String read(final File aFile, final String aEncoding) {
        try {
            return java.nio.file.Files.readString(aFile.toPath(), Charset.forName(aEncoding));
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
