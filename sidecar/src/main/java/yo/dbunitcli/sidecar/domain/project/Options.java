package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record Options(Map<CommandType, List<Path>> parameterFiles) {
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

    public void save(final CommandType type, final Path path) {
        if (this.parameterFiles(type)
                .filter(it -> it.toAbsolutePath().equals(path.toAbsolutePath()))
                .findAny()
                .isEmpty()) {
            this.parameterFiles().put(type, Stream.concat(this.parameterFiles(type), Stream.of(path))
                    .toList());
            Options.LOGGER.info(String.format("type:%s include:%s", type, this.parameterFiles(type).toList()));
        }
    }

    public static class Builder {
        private final Map<CommandType, List<Path>> parameterFiles = new HashMap<>();

        public Builder addParameterFiles(final CommandType type, final List<Path> list) {
            if (!this.parameterFiles.containsKey(type)) {
                this.parameterFiles.put(type, new ArrayList<>());
            }
            this.parameterFiles.get(type).addAll(list);
            return this;
        }

        public Options build() {
            return new Options(new HashMap<>(this.parameterFiles));
        }
    }
}
