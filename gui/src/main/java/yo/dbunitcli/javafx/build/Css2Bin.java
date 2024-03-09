package yo.dbunitcli.javafx.build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Css2Bin {
    public static void main(final String[] args) throws IOException {
        try (final Stream<Path> walk = Files.walk(new File(args[0]).toPath())) {
            walk
                    .map(it -> it.toFile().getAbsolutePath())
                    .filter(it -> it.endsWith(".css"))
                    .forEach(it -> {
                        try {
                            com.sun.javafx.css.parser.Css2Bin.main(new String[]{it});
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
