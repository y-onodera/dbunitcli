package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.Strings;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.converter.CsvConverter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ContextDto;
import yo.dbunitcli.sidecar.dto.ParametersDto;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class Workspace {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);
    private Options options;
    private Resources resources;
    private Path path;

    public static Builder builder() {
        return new Builder();
    }

    private static String[] parameterizeOption(final Type type, final Path templatePath,
                                               final File parameterSource) {
        return new String[]{"-cmd=" + type.name()
                , "-template=" + templatePath.getFileName()
                , "-param.src=" + parameterSource.getName()
                , "-param.srcType=csv"
                , "-unit=record"};
    }

    private Workspace(final Path path, final Options options, final Resources resources) {
        this.path = path;
        this.options = options;
        this.resources = resources;
        System.setProperty(FileResources.PROPERTY_WORKSPACE, this.path().toString());
    }

    public void contextReload(final String workspace, final String datasetBase, final String resultBase) {
        System.setProperty(FileResources.PROPERTY_WORKSPACE, workspace);
        if (Strings.isNotEmpty(datasetBase)) {
            System.setProperty(FileResources.PROPERTY_DATASET_BASE, datasetBase);
        }
        if (Strings.isNotEmpty(resultBase)) {
            System.setProperty(FileResources.PROPERTY_RESULT_BASE, resultBase);
        }
        final var newWorkspace = Workspace.builder().setPath(workspace).build();
        this.options = newWorkspace.options();
        this.resources = newWorkspace.resources();
        this.path = newWorkspace.path();
    }

    public Options options() {
        return this.options;
    }

    public Resources resources() {
        return this.resources;
    }

    public Path path() {
        return this.path;
    }

    public WorkspaceDto toDto() {
        final WorkspaceDto result = new WorkspaceDto();
        final ParametersDto parameters = new ParametersDto();
        parameters.setConvert(this.options().names(Type.convert).toList());
        parameters.setCompare(this.options().names(Type.compare).toList());
        parameters.setGenerate(this.options().names(Type.generate).toList());
        parameters.setRun(this.options().names(Type.run).toList());
        parameters.setParameterize(this.options().names(Type.parameterize).toList());
        result.setParameterList(parameters);
        result.setResources(this.resources().toDto());

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

    public Stream<String> parameterNames(final yo.dbunitcli.application.CommandType type) {
        return this.options().names(type);
    }

    public Stream<Path> parameterFiles(final Type type) {
        return this.options().paths(type);
    }

    public String parameterize(final Type type, final String name) {
        return this.options().select(type, name)
                   .map(source -> {
                       try {
                           final Option.Parameters target = source.toOptionParameters();
                           final Path templatePath = this.options()
                                                         .templates()
                                                         .add(name + ".txt", new CommandParameters(type, target
                                                                 .parameterizeArgs(false)
                                                                 .toArray(String[]::new))
                                                                 .content());
                           File parameterSource = this.saveParameterSources(name, target);
                           return this.options().add(name
                                   , new CommandParameters(Type.parameterize,
                                                           parameterizeOption(type, templatePath,
                                                                              parameterSource)));
                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                   })
                   .orElse("");
    }

    private File saveParameterSources(final String name, final Option.Parameters args) {
        final File resourcesDir = this.path().toFile();
        Map<String, ?> parameters = args.toMap(false);
        final CsvConverter converter = new CsvConverter(resourcesDir, "UTF-8");
        converter.convert(new ComparableTable.Builder(name, parameters.keySet().toArray(new String[0]))
                                  .addRow(parameters.values().toArray(Object[]::new))
                                  .build());
        return new File(resourcesDir, name + ".csv");
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
            Workspace.LOGGER.info("current workspace:{}", baseDir.getAbsolutePath());
            return new Workspace(baseDir.toPath(), options.build(), resources.build());
        }
    }
}
