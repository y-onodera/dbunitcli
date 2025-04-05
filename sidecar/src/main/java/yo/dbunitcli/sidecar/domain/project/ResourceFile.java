package yo.dbunitcli.sidecar.domain.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Optional<String> read(final String name) {
        return this.files.stream()
                .filter(it -> it.getFileName().toString().equals(name))
                .findFirst()
                .map(it -> {
                    try {
                        return Files.readString(it, StandardCharsets.UTF_8);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void update(final String name, final String contents) throws IOException {
        final Path saveTo = this.prepareFileForUpdate(name);
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
    }

    public void delete(final String name) throws IOException {
        final Path filePath = new File(this.parentDir, name).toPath();
        if (!filePath.toFile().exists()) {
            throw new IOException("File not found: " + name);
        }
        Files.delete(filePath);
        this.files.removeIf(path -> path.getFileName().toString().equals(name));
    }

    private Path prepareFileForUpdate(final String name) throws IOException {
        final File file = new File(name);
        if (file.isAbsolute()) {
            return this.create(file);
        }

        return this.create(new File(this.parentDir, name));
    }

    private Path create(final File file) throws IOException {
        final File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            Files.createDirectories(parentFile.toPath());
        }
        final Path saveTo = file.toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
            this.files.add(saveTo);
        }
        return saveTo;
    }

}