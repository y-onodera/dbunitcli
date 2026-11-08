package yo.dbunitcli.sidecar.domain.project;

import org.apache.poi.ss.util.WorkbookUtil;
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
import yo.dbunitcli.dataset.converter.XlsConverter;
import yo.dbunitcli.dataset.converter.XlsxConverter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ContextDto;
import yo.dbunitcli.sidecar.dto.ParameterizeCandidateDto;
import yo.dbunitcli.sidecar.dto.ParameterizeFileDto;
import yo.dbunitcli.sidecar.dto.ParametersDto;
import yo.dbunitcli.sidecar.dto.ResourcesDto;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Workspace {
    private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);

    private static final String TEMPLATE_OPTION = "-template";
    private static final String PARAM_SRC_OPTION = "-param.src";
    /**
     * parameterizeが作るファイルを指すオプション。共有の設定ファイル（-param.setting等）は削除の対象にしないため含めない。
     * cmdParamはパラメータの値でファイル名が決まり、削除時に特定できないため含めない
     */
    private static final List<String> PARAMETERIZE_FILE_OPTIONS = List.of(TEMPLATE_OPTION, PARAM_SRC_OPTION);

    public static Builder builder() {
        return new Builder();
    }

    private static String[] parameterizeOption(final Type type, final Path templatePath,
                                               final File parameterSource, final ParamSource paramSource) {
        final List<String> result = new ArrayList<>(List.of("-cmd=" + type.name()
                , TEMPLATE_OPTION + "=" + templatePath.getFileName()
                , PARAM_SRC_OPTION + "=" + parameterSource.getName()
                , "-param.srcType=" + paramSource.type()));
        if (paramSource.type() == ParamType.csv) {
            result.add("-param.encoding=" + paramSource.encoding());
        }
        result.add("-unit=record");
        return result.toArray(String[]::new);
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

    public void contextReload(final String workspace, final String datasetBase, final String resultBase, final String env) {
        System.setProperty(FileResources.PROPERTY_WORKSPACE, workspace);
        if (Strings.isNotEmpty(datasetBase)) {
            System.setProperty(FileResources.PROPERTY_DATASET_BASE, datasetBase);
        }
        if (Strings.isNotEmpty(resultBase)) {
            System.setProperty(FileResources.PROPERTY_RESULT_BASE, resultBase);
        }
        // 未指定ならworkspace直下のenv.propertiesを使うためプロパティを消す
        if (Strings.isNotEmpty(env)) {
            System.setProperty(FileResources.PROPERTY_ENV, env);
        } else {
            System.clearProperty(FileResources.PROPERTY_ENV);
        }
        FileResources.baseDir().mkdirs();
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
        final ResourcesDto resources = this.resources().toDto();
        resources.setParameterizeTemplateFiles(this.options().templates().list());
        resources.setCompareSettings(this.options().compareSettings().list());
        resources.setGenerateSettings(this.options().generateSettings().list());
        result.setResources(resources);

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
        context.setEnv(Optional.ofNullable(System.getProperty(FileResources.PROPERTY_ENV)).orElse(""));
        final Function<Option.BaseDir, String> base = baseDir -> withRawWorkspacePrefix(
                baseDir.dir().toPath().toAbsolutePath().normalize().toString(), expandedWorkspace, rawWorkspace);
        context.setSettingBase(base.apply(Option.BaseDir.SETTING));
        context.setCompareSettingBase(base.apply(Option.BaseDir.COMPARE_SETTING));
        context.setGenerateSettingBase(base.apply(Option.BaseDir.GENERATE_SETTING));
        context.setTemplateBase(base.apply(Option.BaseDir.TEMPLATE));
        context.setParameterizeTemplateBase(base.apply(Option.BaseDir.PARAMETERIZE_TEMPLATE));
        context.setJdbcBase(base.apply(Option.BaseDir.JDBC));
        context.setXlsxSchemaBase(base.apply(Option.BaseDir.XLSX_SCHEMA));
        context.setFixedColumnDefBase(base.apply(Option.BaseDir.FIXED_COLUMN_DEF));
        context.setSqlBase(base.apply(Option.BaseDir.SQL));
        context.setTableBase(base.apply(Option.BaseDir.TABLE));
        context.setCsvqBase(base.apply(Option.BaseDir.CSVQ));
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
        template.add("envProperty", FileResources.PROPERTY_ENV);
        template.add("env", System.getProperty(FileResources.PROPERTY_ENV));
        template.add("commandType", commandType.name());
        template.add("name", name);
        final String content = template.render();
        final File scriptFile = new File(launchDir.toFile(), commandType.name() + "_" + name + ".bat");
        // BATファイルはWindowsのOSデフォルト文字コードで保存する必要がある
        Files.writeString(scriptFile.toPath(), content, Charset.defaultCharset());
        return scriptFile.getAbsolutePath();
    }

    /**
     * parameterizeの対象に選べるオプション（値が入っているもの）を保存順に返す
     */
    public List<ParameterizeCandidateDto> parameterizeCandidates(final Type type, final String name) {
        return this.options().select(type, name)
                   .map(source -> {
                       final Option.Parameters target = source.toOptionParameters();
                       return target.toList(false, key -> key).stream()
                                    .map(key -> new ParameterizeCandidateDto(key, target.get(key)))
                                    .toList();
                   })
                   .orElse(List.of());
    }

    /**
     * @param parameterizeKeys $param.xxx$に置き換えるオプション。nullなら値が入っている全オプション
     */
    public String parameterize(final Type type, final String name, final List<String> parameterizeKeys
            , final ParamSource paramSource) throws IOException {
        final Optional<CommandParameters> source = this.options().select(type, name);
        if (source.isPresent()) {
            final Option.Parameters target = source.get().toOptionParameters();
            final Predicate<String> parameterize = parameterizeKeys == null ? _ -> true : parameterizeKeys::contains;
            if (target.toList(false, key -> key).stream().noneMatch(parameterize)) {
                throw new IllegalArgumentException("no option selected to parameterize");
            }
            final Path templatePath = this.options()
                    .templates()
                    .add(name + ".txt", new CommandParameters(type, target
                            .parameterizeArgs(false, parameterize)
                            .toArray(String[]::new))
                            .content());
            final File parameterSource = this.saveParameterSources(name, target.toMap(false, parameterize), paramSource);
            return this.options().add(name
                    , new CommandParameters(Type.parameterize,
                            parameterizeOption(type, templatePath,
                                    parameterSource, paramSource)));
        }
        return "";
    }

    /**
     * parameterizeのオプションが参照するファイルのうち、存在するもの
     */
    public List<ParameterizeFileDto> parameterizeFiles(final String name) {
        final Map<String, File> files = this.referencedFiles(name);
        if (files.isEmpty()) {
            return List.of();
        }
        final Map<String, Collection<File>> others = this.options().names(Type.parameterize)
                .filter(other -> !other.equals(name))
                .sorted()
                .collect(Collectors.toMap(other -> other, other -> this.referencedFiles(other).values()
                        , (a, _) -> a, LinkedHashMap::new));
        return files.entrySet().stream()
                .map(entry -> new ParameterizeFileDto(entry.getKey(), entry.getValue().getPath()
                        , others.keySet().stream()
                                .filter(other -> others.get(other).contains(entry.getValue()))
                                .toList()))
                .toList();
    }

    /**
     * parameterizeのオプションと、filesに指定した参照先のファイルを削除する
     *
     * @param files parameterizeFilesのpathのうち、他から参照されていないもの
     */
    public void deleteParameterize(final String name, final List<String> files) throws IOException {
        if (!files.isEmpty()) {
            final Set<String> deletable = this.parameterizeFiles(name).stream()
                    .filter(file -> file.usedBy().isEmpty())
                    .map(ParameterizeFileDto::path)
                    .collect(Collectors.toSet());
            final List<String> notDeletable = files.stream().filter(file -> !deletable.contains(file)).toList();
            if (!notDeletable.isEmpty()) {
                throw new IllegalArgumentException("cannot delete: " + notDeletable);
            }
        }
        this.options().delete(Type.parameterize, name);
        for (final String path : files) {
            final File file = new File(path);
            if (!this.options().templates().deleteIfContains(file)) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }

    private Map<String, File> referencedFiles(final String name) {
        final Map<String, File> result = new LinkedHashMap<>();
        this.options().select(Type.parameterize, name).ifPresent(parameterize -> {
            final Option.Parameters parameters = parameterize.toOptionParameters();
            for (final String key : PARAMETERIZE_FILE_OPTIONS) {
                if (parameters.hasValue(key)) {
                    final File file = parameters.getArg(key).attribute().defaultPath().search(parameters.get(key));
                    if (file.isFile()) {
                        result.put(key, file.toPath().toAbsolutePath().normalize().toFile());
                    }
                }
            }
        });
        return result;
    }

    private Path installDir() {
        // sidecarはbackend/dbunit-cli-sidecar.exeとして配置されるため
        // 自身のexeパスから2階層上(backend/の親)がインストールディレクトリ
        return ProcessHandle.current().info().command()
                .map(cmd -> Path.of(cmd).toAbsolutePath().normalize().getParent())
                .map(Path::getParent)
                .orElseGet(() -> Path.of(System.getProperty("user.dir")));
    }

    private File saveParameterSources(final String name, final Map<String, ?> parameters, final ParamSource paramSource) {
        final File resourcesDir = this.path().toFile();
        if (paramSource.type() == ParamType.csv) {
            new CsvConverter(resourcesDir, paramSource.encoding())
                    .convert(parameterTable(name, parameters));
            return new File(resourcesDir, name + ".csv");
        }
        // テーブル名がシート名になるため、Excelで使えない文字・長さを丸める
        final XlsxConverter converter = new XlsxConverter(resourcesDir, name, XlsConverter.TableExportType.SHEET
                , true, true, false);
        converter.startDataSet();
        converter.convert(parameterTable(WorkbookUtil.createSafeSheetName(name), parameters));
        converter.endDataSet();
        return new File(resourcesDir, name + ".xlsx");
    }

    private static ComparableTable parameterTable(final String tableName, final Map<String, ?> parameters) {
        return new ComparableTable.Builder(tableName, parameters.keySet().toArray(new String[0]))
                .addRow(parameters.values().toArray(Object[]::new))
                .build();
    }

    /**
     * parameterizeのパラメータデータの形式
     *
     * @param type     csv または xlsx
     * @param encoding csvの文字コード
     */
    public record ParamSource(ParamType type, String encoding) {
        public static final ParamSource DEFAULT = new ParamSource(ParamType.csv, "UTF-8");

        /** 未指定の項目はDEFAULTの値にする */
        public ParamSource(final String type, final String encoding) {
            this(Strings.isEmpty(type) ? DEFAULT.type() : ParamType.valueOf(type)
                    , Strings.isEmpty(encoding) ? DEFAULT.encoding() : encoding);
        }

        public ParamSource {
            if (type == ParamType.csv && !Charset.isSupported(encoding)) {
                throw new IllegalArgumentException("unsupported encoding: " + encoding);
            }
        }
    }

    /** 名前は-param.srcTypeの値 */
    public enum ParamType {
        csv, xlsx
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
            options.workspace(expandedBaseDir);
            resources.workspace(expandedBaseDir);
            Workspace.LOGGER.info("current workspace:{}", expandedBaseDir.getAbsolutePath());
            return new Workspace(rawFile.toPath(), options.build(), resources.build());
        }
    }
}
