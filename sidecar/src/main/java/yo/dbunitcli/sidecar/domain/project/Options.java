package yo.dbunitcli.sidecar.domain.project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public record Options(Map<CommandType, List<Path>> parameterFiles) {
    public static Builder builder() {
        return new Builder();
    }

    public Stream<Path> parameterFiles(final CommandType type) {
        return this.parameterFiles().get(type).stream();
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
