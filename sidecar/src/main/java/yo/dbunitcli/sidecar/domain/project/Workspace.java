package yo.dbunitcli.sidecar.domain.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Workspace {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);

    public static File resolveBaseDir(final Option.BaseDir baseDir, final String srcType) {
        return resolveBaseDir(baseDir.name(), srcType);
    }

    public static File resolveBaseDir(final String defaultPath, final String srcType) {
        return switch (defaultPath) {
            case "DATASET" -> Strings.isNotEmpty(srcType)
                    ? new File(FileResources.datasetDir(), srcType)
                    : FileResources.datasetDir();
            case "RESULT" -> FileResources.resultDir();
            case "SETTING" -> FileResources.settingDir();
            case "TEMPLATE" -> FileResources.templateFileDir();
            case "PARAMETERIZE_TEMPLATE" -> FileResources.parameterizeTemplateDir();
            case "JDBC" -> FileResources.jdbcPropDir();
            case "XLSX_SCHEMA" -> FileResources.xlsxSchemaDir();
            default -> FileResources.baseDir();
        };
    }

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

    private static String toRelative(final Path base, final Path target) {
        try {
            return base.relativize(target).toString();
        } catch (final IllegalArgumentException e) {
            return target.toString();
        }
    }

    private static String rawOrRelative(final Path launchDir, final String propertyName, final Supplier<Path> expandedPath) {
        final String raw = System.getProperty(propertyName);
        if (raw != null && raw.contains("%")) {
            return raw;
        }
        return toRelative(launchDir, expandedPath.get());
    }

    private Options options;
    private Resources resources;
    private Path path;

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
        final Path baseDirPath = FileResources.baseDir().toPath();
        final String rawWorkspace = rawOrAbsolute(FileResources.PROPERTY_WORKSPACE, () -> baseDirPath);
        final String expandedWorkspace = baseDirPath.toAbsolutePath().normalize().toString();
        context.setWorkspace(rawWorkspace);
        context.setDatasetBase(rawOrAbsolute(FileResources.PROPERTY_DATASET_BASE,
                () -> FileResources.datasetDir().toPath()));
        context.setResultBase(rawOrAbsolute(FileResources.PROPERTY_RESULT_BASE,
                () -> FileResources.resultDir().toPath()));
        context.setSettingBase(withRawWorkspacePrefix(
                FileResources.settingDir().toPath().toAbsolutePath().normalize().toString(),
                expandedWorkspace, rawWorkspace));
        context.setTemplateBase(withRawWorkspacePrefix(
                FileResources.templateFileDir().toPath().toAbsolutePath().normalize().toString(),
                expandedWorkspace, rawWorkspace));
        context.setParameterizeTemplateBase(withRawWorkspacePrefix(
                FileResources.parameterizeTemplateDir().toPath().toAbsolutePath().normalize().toString(),
                expandedWorkspace, rawWorkspace));
        context.setJdbcBase(withRawWorkspacePrefix(
                FileResources.jdbcPropDir().toPath().toAbsolutePath().normalize().toString(),
                expandedWorkspace, rawWorkspace));
        context.setXlsxSchemaBase(withRawWorkspacePrefix(
                FileResources.xlsxSchemaDir().toPath().toAbsolutePath().normalize().toString(),
                expandedWorkspace, rawWorkspace));
        return result;
    }

    private static String rawOrAbsolute(final String propertyName, final Supplier<Path> expandedPath) {
        final String raw = System.getProperty(propertyName);
        if (raw != null && raw.contains("%")) {
            return raw;
        }
        return expandedPath.get().toAbsolutePath().normalize().toString();
    }

    private static String withRawWorkspacePrefix(
            final String absPath, final String expandedWorkspace, final String rawWorkspace) {
        if (rawWorkspace.contains("%") && absPath.startsWith(expandedWorkspace)) {
            return rawWorkspace + absPath.substring(expandedWorkspace.length());
        }
        return absPath;
    }

    public Stream<String> parameterNames(final yo.dbunitcli.application.CommandType type) {
        return this.options().names(type);
    }

    public Stream<Path> parameterFiles(final Type type) {
        return this.options().paths(type);
    }

    public String saveShell(final Type commandType, final String name) throws IOException {
        final Path launchDir = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        final Path backendDir = this.installDir().resolve("backend");
        final STGroup group = new STGroupFile("shell/saveShell.stg", '$', '$');
        final ST template = group.getInstanceOf("saveShell");
        template.add("sidecarExe", toRelative(launchDir, backendDir.resolve("dbunit-cli-sidecar.exe")));
        template.add("backendDir", toRelative(launchDir, backendDir));
        template.add("workspaceProperty", FileResources.PROPERTY_WORKSPACE);
        template.add("workspace", rawOrRelative(launchDir, FileResources.PROPERTY_WORKSPACE, () -> FileResources.baseDir().toPath().toAbsolutePath().normalize()));
        template.add("datasetBaseProperty", FileResources.PROPERTY_DATASET_BASE);
        template.add("datasetBase", rawOrRelative(launchDir, FileResources.PROPERTY_DATASET_BASE, () -> FileResources.datasetDir().toPath().toAbsolutePath().normalize()));
        template.add("resultBaseProperty", FileResources.PROPERTY_RESULT_BASE);
        template.add("resultBase", rawOrRelative(launchDir, FileResources.PROPERTY_RESULT_BASE, () -> FileResources.resultDir().toPath().toAbsolutePath().normalize()));
        template.add("commandType", commandType.name());
        template.add("name", name);
        final String content = template.render();
        final File scriptFile = new File(launchDir.toFile(), commandType.name() + "_" + name + ".bat");
        // BATファイルはWindowsのOSデフォルト文字コードで保存する必要がある
        Files.writeString(scriptFile.toPath(), content, Charset.defaultCharset());
        return scriptFile.getAbsolutePath();
    }

    public String parameterize(final Type type, final String name) throws IOException {
        Optional<CommandParameters> source = this.options().select(type, name);
        if (source.isPresent()) {
            final Option.Parameters target = source.get().toOptionParameters();
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
        }
        return "";
    }

    private Path installDir() {
        // sidecarはbackend/dbunit-cli-sidecar.exeとして配置されるため
        // 自身のexeパスから2階層上(backend/の親)がインストールディレクトリ
        return ProcessHandle.current().info().command()
                .map(cmd -> Path.of(cmd).toAbsolutePath().normalize().getParent())
                .map(Path::getParent)
                .orElseGet(() -> Path.of(System.getProperty("user.dir")));
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
            return Strings.isEmpty(this.path) ? ".":this.path;
        }

        public Builder setPath(final String path) {
            this.path = path;
            return this;
        }

        public Workspace build() {
            final File rawFile = new File(this.getPath());
            System.setProperty(FileResources.PROPERTY_WORKSPACE, this.getPath());
            final File expandedBaseDir = FileResources.baseDir();
            final Resources.Builder resources = Resources.builder();
            final Options.Builder options = Options.builder();
            if (expandedBaseDir.exists() || expandedBaseDir.mkdirs()) {
                options.workspace(expandedBaseDir);
                resources.workspace(expandedBaseDir);
            }
            Workspace.LOGGER.info("current workspace:{}", expandedBaseDir.getAbsolutePath());
            return new Workspace(rawFile.toPath(), options.build(), resources.build());
        }
    }
}
