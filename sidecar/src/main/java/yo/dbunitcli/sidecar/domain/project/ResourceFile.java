package yo.dbunitcli.sidecar.domain.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record ResourceFile(File baseDir, Set<String> files) {

    public ResourceFile(final File parentDir) {
        this(parentDir, new TreeSet<>());
        this.reload();
    }

    public Optional<String> read(final String name) {
        return this.select(name).map(it -> {
            try {
                return Files.readString(it, StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Optional<Path> select(String target) {
        return this.files.stream().filter(it -> it.equals(target)).findFirst()
                .map(it -> new File(this.baseDir, it).toPath());
    }

    public List<String> list() {
        return this.files.stream().toList();
    }

    public Stream<String> stream() {
        return this.files.stream();
    }

    public Stream<Path> pathStream() {
        return this.files.stream().map(it -> new File(this.baseDir, it).toPath());
    }

    public Path add(final String name, final String contents) throws IOException {
        final Path saveTo = this.prepareUpdate(this.getUniqueName(name));
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
        return saveTo;
    }

    public void update(final String name, final String contents) throws IOException {
        final Path saveTo = this.prepareUpdate(name);
        Files.writeString(saveTo, contents, StandardCharsets.UTF_8);
    }

    public void delete(final String name) throws IOException {
        final Path filePath = new File(this.baseDir, name).toPath();
        if (!filePath.toFile().exists()) {
            throw new IOException("File not found: " + name);
        }
        Files.deleteIfExists(filePath);
        this.files.removeIf(path -> path.equals(name));
    }

    public void copy(final String source) throws IOException {
        Optional<String> content = this.read(source);
        if (content.isPresent()) {
            this.add(source, content.get());
        }
    }

    public void rename(final String current, final String newName) throws IOException {
        Optional<Path> path = this.select(current);
        if (path.isPresent()) {
            String toUnique = this.getUniqueName(newName);
            Path newPath = new File(this.baseDir, toUnique).toPath();
            Files.move(path.get(), newPath, StandardCopyOption.REPLACE_EXISTING);
            this.files.remove(current);
            this.files.add(toUnique);
        }
    }

    private String getUniqueName(final String name) {
        if (!this.files.contains(name)) {
            return name;
        }
        final int dotIndex = name.lastIndexOf('.');
        final String base = dotIndex >= 0 ? name.substring(0, dotIndex):name;
        final String ext = dotIndex >= 0 ? name.substring(dotIndex):"";
        return IntStream.iterate(1, i -> i + 1)
                .mapToObj(i -> base + "(" + i + ")" + ext)
                .filter(candidate -> !this.files.contains(candidate))
                .findFirst()
                .orElse("");
    }

    private void reload() {
        if (this.baseDir.exists() && this.baseDir.isDirectory()) {
            this.files.clear();
            try (final Stream<Path> pathStream = Files.walk(this.baseDir.toPath(), 1)) {
                this.files.addAll(
                        pathStream.filter(it -> it.toFile().isFile()).map(it -> it.getFileName().toString()).toList());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path create(final File file) throws IOException {
        final File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            Files.createDirectories(parentFile.toPath());
        }
        final Path saveTo = file.toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
            this.files.add(saveTo.getFileName().toString());
        }
        return saveTo;
    }

    private Path prepareUpdate(final String name) throws IOException {
        final File file = new File(name);
        if (file.isAbsolute()) {
            return this.create(file);
        }
        return this.create(new File(this.baseDir, name));
    }

}