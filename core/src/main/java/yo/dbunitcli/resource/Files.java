package yo.dbunitcli.resource;

import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public enum Files {
    SINGLETON;


    public static String readClasspathResource(final String aURL) {
        return readClasspathResource(aURL, StandardCharsets.UTF_8);
    }

    public static String readClasspathResource(final String aURL, final Charset aCharset) {
        try {
            return Resources.asCharSource(Objects.requireNonNull(Files.class
                                    .getClassLoader()
                                    .getResource(aURL))
                            , aCharset)
                    .read();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    public static String read(final File aFile, final String aEncoding) {
        try {
            return com.google.common.io.Files
                    .asCharSource(aFile, Charset.forName(aEncoding))
                    .read();
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
