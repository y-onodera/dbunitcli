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
        , List<Path> xlsxSchema) {

    public static Builder builder() {
        return new Builder();
    }

    private static File getSettingDir(final File baseDir) {
        return new File(baseDir, "setting");
    }

    private static File getXlsxSchemaDir(final File baseDir) {
        return new File(baseDir, "xlsxSchema");
    }

    public String metadataSetting(final String name) {
        return this.readFileContents(this.metadataSetting(), name);
    }

    public String xlsxSchema(final String name) {
        return this.readFileContents(this.xlsxSchema(), name);
    }

    public void updateSetting(final String name, final String contents) throws IOException {
        final Path saveTo = this.prepareFileForUpdate(name, getSettingDir(this.baseDir), this.metadataSetting);
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
    }

    public void updateXlsxSchema(final String name, final String contents) throws IOException {
        final Path saveTo = this.prepareFileForUpdate(name, getXlsxSchemaDir(this.baseDir), this.xlsxSchema);
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
    }

    private String readFileContents(final List<Path> list, final String name) {
        return list.stream()
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

    private Path prepareFileForUpdate(final String name, final File parentDir, final List<Path> list) throws IOException {
        if (!parentDir.exists()) {
            Files.createDirectories(parentDir.toPath());
        }
        final Path saveTo = new File(parentDir, name).toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
            list.add(saveTo);
        }
        return saveTo;
    }

    public static class Builder {
        private List<String> jdbc = new ArrayList<>();
        private List<Path> metadataSetting = new ArrayList<>();
        private List<String> template = new ArrayList<>();
        private List<Path> xlsxSchema = new ArrayList<>();
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
            this.addFilesToList(getSettingDir(this.baseDir), this.metadataSetting);
            this.addFilesToList(getXlsxSchemaDir(this.baseDir), this.xlsxSchema);
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

        public Builder setXlsxSchema(final List<Path> xlsxSchema) {
            this.xlsxSchema = xlsxSchema;
            return this;
        }

        public Builder setBaseDir(final File baseDir) {
            this.baseDir = baseDir;
            return this;
        }

        private void addFilesToList(final File dir, final List<Path> list) {
            if (dir.exists() && dir.isDirectory()) {
                try (final Stream<Path> pathStream = Files.walk(dir.toPath(), 1)) {
                    list.addAll(pathStream
                            .filter(it -> it.toFile().isFile())
                            .toList());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
