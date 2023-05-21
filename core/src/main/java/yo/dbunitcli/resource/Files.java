package yo.dbunitcli.resource;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

public enum Files {
    SINGLETON;


    public static String readClasspathResource(final String aURL) {
        return readClasspathResource(aURL, StandardCharsets.UTF_8);
    }

    public static String readClasspathResource(final String aURL, final Charset aCharset) {
        try {
            return java.nio.file.Files.readString(Path.of(Objects.requireNonNull(Files.class.getClassLoader()
                    .getResource(aURL)).toURI()), aCharset);
        } catch (final IOException | URISyntaxException e) {
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
