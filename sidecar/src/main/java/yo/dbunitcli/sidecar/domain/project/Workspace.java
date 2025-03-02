package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ContextDto;
import yo.dbunitcli.sidecar.dto.ParametersDto;
import yo.dbunitcli.sidecar.dto.ResourcesDto;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public record Workspace(Path path, Options options, Resources resources) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);

    public static Workspace contextReload(final String workspace, final String datasetBase, final String resultBase) {
        System.setProperty(FileResources.PROPERTY_WORKSPACE, workspace);
        if (Strings.isNotEmpty(datasetBase)) {
            System.setProperty(FileResources.PROPERTY_DATASET_BASE, datasetBase);
        }
        if (Strings.isNotEmpty(resultBase)) {
            System.setProperty(FileResources.PROPERTY_RESULT_BASE, resultBase);
        }
        return Workspace.builder().setPath(workspace).build();
    }

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
        resources.setDatasetSettings(this.metadataSettings());
        result.setResources(resources);
        final ContextDto context = new ContextDto();
        result.setContext(context);
        context.setWorkspace(FileResources.baseDir().toPath().toAbsolutePath().normalize().toString());
        context.setDatasetBase(FileResources.datasetDir().toPath().toAbsolutePath().normalize().toString());
        context.setResultBase(FileResources.resultDir().toPath().toAbsolutePath().normalize().toString());
        context.setSettingBase(FileResources.settingDir().toPath().toAbsolutePath().normalize().toString());
        context.setTemplateBase(FileResources.templateFileDir().toPath().toAbsolutePath().normalize().toString());
        context.setJdbcBase(FileResources.jdbcPropDir().toPath().toAbsolutePath().normalize().toString());
        context.setXlsxSchemaBase(FileResources.xlsxSchemaDir().toPath().toAbsolutePath().normalize().toString());
        return result;
    }

    public Stream<String> parameterNames(final CommandType type) {
        return this.options().parameterNames(type);
    }

    public Stream<Path> parameterFiles(final CommandType type) {
        return this.options().parameterFiles(type);
    }

    public List<String> metadataSettings() {
        return this.resources()
                .metadataSetting()
                .stream()
                .map(it -> it.getFileName().toString())
                .toList();
    }

    public String metadataSetting(final String name) {
        return this.resources().metadataSetting(name);
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
