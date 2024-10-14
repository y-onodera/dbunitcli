package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.sidecar.dto.ParametersDto;
import yo.dbunitcli.sidecar.dto.ResourcesDto;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public record Workspace(Path path, Options options, Resources resources) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);

    public static Builder builder() {
        return new Builder();
    }

    public WorkspaceDto toDto() {
        final WorkspaceDto result = new WorkspaceDto();
        final ParametersDto parameters = new ParametersDto();
        parameters.setConvert(this.options().parameterNames(CommandType.convert).toList());
        parameters.setCompare(this.options().parameterNames(CommandType.compare).toList());
        parameters.setGenerate(this.options().parameterNames(CommandType.generate).toList());
        parameters.setRun(this.options().parameterNames(CommandType.run).toList());
        parameters.setParameterize(this.options().parameterNames(CommandType.parameterize).toList());
        result.setParameterList(parameters);
        final ResourcesDto resources = new ResourcesDto();
        resources.setDatasetSettings(this.settings());
        result.setResources(resources);
        return result;
    }

    public Stream<String> parameterNames(final CommandType type) {
        return this.options().parameterNames(type);
    }

    public Stream<Path> parameterFiles(final CommandType type) {
        return this.options().parameterFiles(type);
    }

    public List<String> settings() {
        return this.resources()
                .setting()
                .stream()
                .map(it -> it.getFileName().toString())
                .toList();
    }

    public String setting(final String name) {
        return this.resources().setting(name);
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
                resources.workspace(baseDir);
            }
            Workspace.LOGGER.info(String.format("current workspace:%s", baseDir.getAbsolutePath()));
            return new Workspace(baseDir.toPath(), options.build(), resources.build());
        }
    }
}
