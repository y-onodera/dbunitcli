package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record Options(File baseDir, Map<yo.dbunitcli.application.CommandType, ResourceFile> parameters,
                      ResourceFile templates) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);

    public static Builder builder() {
        return new Builder();
    }

    public Stream<Path> paths(final Type type) {
        return this.parameters().getOrDefault(type, new ResourceFile(this.getParent(type))).pathStream();
    }

    public Stream<String> names(final yo.dbunitcli.application.CommandType type) {
        return this.parameters.get(type).stream().map(it -> it.replaceAll(".txt", ""));
    }

    public Optional<CommandParameters> select(final yo.dbunitcli.application.CommandType type, final String name) {
        return this.parameters.get(type)
                .select(name + ".txt")
                .map(path -> {
                    try {
                        return new CommandParameters(type, path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public String add(final String name, final CommandParameters commandParameters) throws IOException {
        return this.parameters.get(commandParameters.type())
                .add(name + ".txt", commandParameters.content())
                .getFileName()
                .toString()
                .replace(".txt", "");
    }

    public void newItem(final Type type) throws IOException {
        this.add("new item", new CommandParameters(type));
        this.loggingCurrentFiles(type);
    }

    public void update(final String name, final CommandParameters commandParameters) throws IOException {
        this.parameters.get(commandParameters.type())
                .update(name + ".txt", commandParameters.content());
    }

    public void delete(final Type type, final String name) throws IOException {
        this.parameters.get(type).delete(name + ".txt");
        this.loggingCurrentFiles(type);
    }

    public void rename(final Type type, final String oldName, final String newName) throws IOException {
        this.parameters.get(type).rename(oldName + ".txt", newName + ".txt");
        this.loggingCurrentFiles(type);
    }

    public void copy(final Type type, final String target) throws IOException {
        this.parameters.get(type).copy(target + ".txt");
        this.loggingCurrentFiles(type);
    }

    private File getParent(final Type type) {
        return new File(this.baseDir, type.name());
    }

    private void loggingCurrentFiles(final Type type) {
        Options.LOGGER.info("type:{} include:{}", type, this.paths(type).toList());
    }

    public static class Builder {

        private File baseDir;
        private final Map<yo.dbunitcli.application.CommandType, ResourceFile> parameters = new HashMap<>();
        private ResourceFile templates;

        public Options build() {
            return new Options(this.baseDir, new HashMap<>(this.parameters), this.templates);
        }

        public void workspace(final File workspace) {
            this.baseDir = new File(workspace, "option");
            if (this.baseDir.exists() || this.baseDir.mkdirs()) {
                this.addParameterFiles(Type.compare)
                        .addParameterFiles(Type.convert)
                        .addParameterFiles(Type.generate)
                        .addParameterFiles(Type.run)
                        .addParameterFiles(Type.parameterize)
                ;
                this.templates = new ResourceFile(
                        new File(this.parameters.get(Type.parameterize).baseDir(), "template"));
            }
        }

        private Builder addParameterFiles(final Type command) {
            this.parameters.put(command, new ResourceFile(new File(this.baseDir, command.name())));
            return this;
        }
    }
}
