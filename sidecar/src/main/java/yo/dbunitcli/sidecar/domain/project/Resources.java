package yo.dbunitcli.sidecar.domain.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record Resources(
        File baseDir
        , List<String> jdbc
        , List<Path> setting
        , List<String> template
        , List<String> xlsxSchema) {

    public static Builder builder() {
        return new Builder();
    }

    public String setting(final String name) {
        return this.setting.stream()
                .filter(it -> it.getFileName().toString().equals(name))
                .findFirst()
                .map(it -> {
                    try {
                        return Files.readString(it, StandardCharsets.UTF_8);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse("{}");
    }

    public static class Builder {
        private final List<String> jdbc = new ArrayList<>();
        private final List<Path> setting = new ArrayList<>();
        private final List<String> template = new ArrayList<>();
        private final List<String> xlsxSchema = new ArrayList<>();
        private File baseDir;

        public Resources build() {
            return new Resources(this.baseDir
                    , new ArrayList<>(this.jdbc)
                    , new ArrayList<>(this.setting)
                    , new ArrayList<>(this.template)
                    , new ArrayList<>(this.xlsxSchema)
            );
        }

        public void workspace(final File workspace) {
            this.baseDir = new File(workspace, "resources");
            final File subDir = new File(this.baseDir, "setting");
            if (subDir.exists() && subDir.isDirectory()) {
                try (final Stream<Path> pathStream = Files.walk(subDir.toPath(), 1)) {
                    this.setting.addAll(pathStream
                            .filter(it -> it.toFile().isFile())
                            .toList());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
