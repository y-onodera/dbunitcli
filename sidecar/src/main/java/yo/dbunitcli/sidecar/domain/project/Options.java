package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record Options(File baseDir, Map<CommandType, ResourceFile> parameters, ResourceFile templates) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Options.class);

    public static Builder builder() {
        return new Builder();
    }

    public Stream<Path> paths(final CommandType type) {
        return this.parameters().getOrDefault(type, new ResourceFile(this.getParent(type))).pathStream();
    }

    public Stream<String> names(final CommandType type) {
        return this.parameters.get(type).stream().map(it -> it.replaceAll(".txt", ""));
    }

    public Optional<CommandParameters> select(final CommandType type, final String name) {
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

    public void newItem(final CommandType type) throws IOException {
        this.add("new item", new CommandParameters(type));
        this.loggingCurrentFiles(type);
    }

    public void update(final String name, final CommandParameters commandParameters) throws IOException {
        this.parameters.get(commandParameters.type())
                       .update(name + ".txt", commandParameters.content());
    }

    public void delete(final CommandType type, final String name) throws IOException {
        this.parameters.get(type).delete(name + ".txt");
        this.loggingCurrentFiles(type);
    }

    public void rename(final CommandType type, final String oldName, final String newName) {
        this.parameters.get(type).rename(oldName + ".txt", newName + ".txt");
        this.loggingCurrentFiles(type);
    }

    public void copy(final CommandType type, final String target) {
        this.parameters.get(type).copy(target + ".txt");
        this.loggingCurrentFiles(type);
    }

    private File getParent(final CommandType type) {
        return new File(this.baseDir, type.name());
    }

    private void loggingCurrentFiles(final CommandType type) {
        Options.LOGGER.info("type:{} include:{}", type, this.paths(type).toList());
    }

    public static class Builder {

        private File baseDir;
        private final Map<CommandType, ResourceFile> parameters = new HashMap<>();
        private ResourceFile templates;

        public Options build() {
            return new Options(this.baseDir, new HashMap<>(this.parameters), this.templates);
        }

        public void workspace(final File workspace) {
            this.baseDir = new File(workspace, "option");
            if (this.baseDir.exists() || this.baseDir.mkdirs()) {
                this.addParameterFiles(CommandType.compare)
                        .addParameterFiles(CommandType.convert)
                        .addParameterFiles(CommandType.generate)
                        .addParameterFiles(CommandType.run)
                        .addParameterFiles(CommandType.parameterize)
                ;
                this.templates = new ResourceFile(
                        new File(this.parameters.get(CommandType.parameterize).baseDir(), "template"));
            }
        }

        private Builder addParameterFiles(final CommandType command) {
            this.parameters.put(command, new ResourceFile(new File(this.baseDir, command.name())));
            return this;
        }
    }
}
