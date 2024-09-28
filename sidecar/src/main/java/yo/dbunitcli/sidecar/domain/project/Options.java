package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Options(File baseDir, Map<CommandType, List<Path>> parameterFiles) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);

    public static Builder builder() {
        return new Builder();
    }

    public Stream<Path> parameterFiles(final CommandType type) {
        return this.parameterFiles().getOrDefault(type, new ArrayList<>()).stream();
    }

    public Stream<String> parameterNames(final CommandType type) {
        return this.parameterFiles(type).map(it -> it.toFile().getName().replaceAll(".txt", ""));
    }

    public void delete(final CommandType type, final String name) throws IOException {
        final File parent = this.getParent(type);
        if (parent.exists()) {
            final Path deleteTo = new File(parent, name + ".txt").toPath();
            Files.deleteIfExists(deleteTo);
            this.parameterFiles().put(type, this.parameterFiles(type)
                    .filter(it -> !it.toAbsolutePath().equals(deleteTo.toAbsolutePath()))
                    .toList());
            Options.LOGGER.info(String.format("type:%s include:%s", type, this.parameterFiles(type).toList()));
        }
    }

    public void rename(final CommandType type, final String oldName, final String newName) {
        final Path dest = new File(this.getParent(type), this.getUniqueName(type, newName) + ".txt").toPath();
        if (this.parameterFiles(type)
                .filter(it -> it.getFileName().toString().equals(oldName + ".txt"))
                .map(it -> {
                    try {
                        return Files.move(it, dest);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findAny()
                .isPresent()) {
            this.parameterFiles().put(type, this.parameterFiles(type)
                    .map(it -> {
                        if (it.getFileName().toString().equals(oldName + ".txt")) {
                            return dest;
                        }
                        return it;
                    })
                    .toList());
            Options.LOGGER.info(String.format("type:%s include:%s", type, this.parameterFiles(type).toList()));
        }
    }

    public void add(final CommandType type, final String name, final String[] args) throws IOException {
        this.save(type, this.getUniqueName(type, name), args);
    }

    public void update(final CommandType type, final String name, final String[] args) throws IOException {
        this.save(type, name, args);
    }

    public void save(final CommandType type, final String name, final String[] args) throws IOException {
        final File parent = this.getParent(type);
        if (!parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
        final Path saveTo = new File(parent, name + ".txt").toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
        }
        Files.writeString(saveTo, String.join("\r\n", args), Charset.forName(System.getProperty("file.encoding")));
        if (this.parameterFiles(type)
                .filter(it -> it.toAbsolutePath().equals(saveTo.toAbsolutePath()))
                .findAny()
                .isEmpty()) {
            this.parameterFiles().put(type, Stream.concat(this.parameterFiles(type), Stream.of(saveTo))
                    .toList());
            Options.LOGGER.info(String.format("type:%s include:%s", type, this.parameterFiles(type).toList()));
        }
    }

    private File getParent(final CommandType type) {
        return new File(this.baseDir, type.name());
    }

    private String getUniqueName(final CommandType type, final String name) {
        final List<String> alreadyExists = this.parameterNames(type)
                .filter(it -> Pattern.compile(Pattern.quote(name) + "(\\([0-9]+\\))*").matcher(it).matches())
                .toList();
        return alreadyExists.size() == 0
                ? name
                : IntStream.iterate(1, it -> it + 1)
                .filter(it -> !alreadyExists.contains(name + "(%s)".formatted(it)))
                .mapToObj((name + "(%s)")::formatted)
                .findFirst()
                .get();
    }

    public static class Builder {

        private final Map<CommandType, List<Path>> parameterFiles = new HashMap<>();
        private File baseDir;

        public Options build() {
            return new Options(this.baseDir, new HashMap<>(this.parameterFiles));
        }

        public void workspace(final File workspace) {
            this.baseDir = new File(workspace, "option");
            if (this.baseDir.exists()) {
                this.addParameterFiles(CommandType.compare)
                        .addParameterFiles(CommandType.convert)
                        .addParameterFiles(CommandType.generate)
                        .addParameterFiles(CommandType.run)
                        .addParameterFiles(CommandType.parameterize)
                ;
            }
        }

        private Builder addParameterFiles(final CommandType command, final List<Path> list) {
            if (!this.parameterFiles.containsKey(command)) {
                this.parameterFiles.put(command, new ArrayList<>());
            }
            this.parameterFiles.get(command).addAll(list);
            return this;
        }

        private Builder addParameterFiles(final CommandType command) {
            final File subDir = new File(this.baseDir, command.name());
            final List<Path> files = new ArrayList<>();
            if (subDir.exists() && subDir.isDirectory()) {
                try (final Stream<Path> pathStream = Files.walk(subDir.toPath(), 1)) {
                    files.addAll(pathStream
                            .filter(it -> it.toFile().isFile() && !it.toAbsolutePath().toString().equals(subDir.getAbsolutePath()))
                            .toList());
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return this.addParameterFiles(command, files);
        }
    }
}
