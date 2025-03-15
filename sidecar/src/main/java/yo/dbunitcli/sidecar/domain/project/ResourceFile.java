package yo.dbunitcli.sidecar.domain.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ResourceFile {
    private final List<Path> files = new ArrayList<>();
    private final File parentDir;

    public ResourceFile(final File parentDir) {
        this.parentDir = parentDir;
        if (this.parentDir.exists() && this.parentDir.isDirectory()) {
            try (final Stream<Path> pathStream = Files.walk(this.parentDir.toPath(), 1)) {
                this.files.addAll(pathStream
                        .filter(it -> it.toFile().isFile())
                        .toList());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<String> list() {
        return this.files.stream()
                .map(it -> it.getFileName().toString())
                .toList();
    }

    public String read(final String name) {
        return this.files.stream()
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
        final Path saveTo = this.prepareFileForUpdate(name);
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
    }

    private Path prepareFileForUpdate(final String name) throws IOException {
        if (!this.parentDir.exists()) {
            Files.createDirectories(this.parentDir.toPath());
        }
        final Path saveTo = new File(this.parentDir, name).toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
            this.files.add(saveTo);
        }
        return saveTo;
    }

}