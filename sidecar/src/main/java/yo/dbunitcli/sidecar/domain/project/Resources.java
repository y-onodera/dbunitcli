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
        , List<Path> metadataSetting
        , List<String> template
        , List<String> xlsxSchema) {

    public static Builder builder() {
        return new Builder();
    }

    private static File getSettingDir(final File baseDir1) {
        return new File(baseDir1, "setting");
    }

    public String metadataSetting(final String name) {
        return this.metadataSetting().stream()
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

    public void update(final String name, final String contents) throws IOException {
        final File parent = getSettingDir(this.baseDir());
        if (!parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        final Path saveTo = new File(parent, name).toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
            this.metadataSetting.add(saveTo);
        }
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
    }

    public static class Builder {
        private List<String> jdbc = new ArrayList<>();
        private List<Path> metadataSetting = new ArrayList<>();
        private List<String> template = new ArrayList<>();
        private List<String> xlsxSchema = new ArrayList<>();
        private File baseDir;

        public Resources build() {
            return new Resources(this.baseDir
                    , new ArrayList<>(this.jdbc)
                    , new ArrayList<>(this.metadataSetting)
                    , new ArrayList<>(this.template)
                    , new ArrayList<>(this.xlsxSchema)
            );
        }

        public void workspace(final File workspace) {
            this.baseDir = new File(workspace, "resources");
            final File subDir = getSettingDir(this.baseDir);
            if (subDir.exists() && subDir.isDirectory()) {
                try (final Stream<Path> pathStream = Files.walk(subDir.toPath(), 1)) {
                    this.metadataSetting.addAll(pathStream
                            .filter(it -> it.toFile().isFile())
                            .toList());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public Builder setJdbc(final List<String> jdbc) {
            this.jdbc = jdbc;
            return this;
        }

        public Builder setMetadataSetting(final List<Path> metadataSetting) {
            this.metadataSetting = metadataSetting;
            return this;
        }

        public Builder setTemplate(final List<String> template) {
            this.template = template;
            return this;
        }

        public Builder setXlsxSchema(final List<String> xlsxSchema) {
            this.xlsxSchema = xlsxSchema;
            return this;
        }

        public Builder setBaseDir(final File baseDir) {
            this.baseDir = baseDir;
            return this;
        }
    }

}
