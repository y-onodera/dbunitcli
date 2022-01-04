package yo.dbunitcli.resource;

import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public enum Files {
    SINGLETON;


    public static String readClasspathResource(String aURL) throws URISyntaxException, IOException {
        return readClasspathResource(aURL, StandardCharsets.UTF_8);
    }

    public static String readClasspathResource(String aURL, Charset aCharset) throws IOException {
        return Resources.asCharSource(Objects.requireNonNull(Files.class
                        .getClassLoader()
                        .getResource(aURL))
                , aCharset)
                .read();
    }

    public static String read(File aFile, String aEncoding) throws IOException {
        return com.google.common.io.Files
                .asCharSource(aFile, Charset.forName(aEncoding))
                .read();
    }
}
