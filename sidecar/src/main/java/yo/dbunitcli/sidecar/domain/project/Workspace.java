package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.sidecar.dto.ParametersDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public record Workspace(Path path, Options options, Resources resources) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);

    public static Builder builder() {
        return new Builder();
    }

    public ParametersDto parameterFiles() {
        final ParametersDto result = new ParametersDto();
        result.setConvert(this.options().parameterNames(CommandType.convert).toList());
        result.setCompare(this.options().parameterNames(CommandType.compare).toList());
        result.setGenerate(this.options().parameterNames(CommandType.generate).toList());
        result.setRun(this.options().parameterNames(CommandType.run).toList());
        result.setParameterize(this.options().parameterNames(CommandType.parameterize).toList());
        return result;
    }

    public Stream<String> parameterNames(final CommandType type) {
        return this.options().parameterNames(type);
    }

    public Stream<Path> parameterFiles(final CommandType type) {
        return this.options().parameterFiles(type);
    }

    public void save(final CommandType type, final String name, final String[] args) throws IOException {
        this.options().save(type, name, args);
    }

    public void delete(final CommandType type, final String name) throws IOException {
        this.options().delete(type, name);
    }

    public void rename(final CommandType type, final String oldName, final String newName) {
        this.options().rename(type, oldName, newName);
    }

    public static class Builder {

        private String path;

        public String getPath() {
            return Strings.isEmpty(this.path) ? "." : this.path;
        }

        public Builder setPath(final String path) {
            this.path = path;
            return this;
        }

        public Workspace build() {
            final File baseDir = new File(this.getPath());
            final Resources.Builder resources = Resources.builder();
            final Options.Builder options = Options.builder();
            if (baseDir.exists() || baseDir.mkdirs()) {
                options.workspace(baseDir);
            }
            Workspace.LOGGER.info(String.format("current workspace:%s", baseDir.getAbsolutePath()));
            return new Workspace(baseDir.toPath(), options.build(), resources.build());
        }
    }
}
