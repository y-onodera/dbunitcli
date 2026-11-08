package yo.dbunitcli.sidecar.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.CommandParameters;
import yo.dbunitcli.application.command.Parameterize;
import yo.dbunitcli.application.command.Type;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ParameterizeCandidateDto;
import yo.dbunitcli.sidecar.dto.ParameterizeFileDto;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class WorkspaceTest {
    @TempDir
    private Path tempDir;

    private Workspace workspace;

    @BeforeEach
    void setUp() {
        System.clearProperty(FileResources.PROPERTY_WORKSPACE);
        System.clearProperty(FileResources.PROPERTY_DATASET_BASE);
        System.clearProperty(FileResources.PROPERTY_RESULT_BASE);
        System.clearProperty(FileResources.PROPERTY_ENV);
        this.workspace = Workspace.builder()
                .setPath(this.tempDir.toString())
                .build();
    }

    @Test
    void contextReload_プロパティが正しく設定される() {
        // Arrange
        final String newWorkspace = this.tempDir.resolve("new").toString();
        final String datasetBase = "dataset";
        final String resultBase = "result";
        final String env = "env/staging.properties";

        // Act
        this.workspace.contextReload(newWorkspace, datasetBase, resultBase, env);

        // Assert
        final WorkspaceDto dto = this.workspace.toDto();
        final var context = dto.getContext();
        assertEquals(newWorkspace, context.getWorkspace());
        assertEquals(new File(datasetBase).getAbsolutePath(), context.getDatasetBase());
        assertEquals(new File(resultBase).getAbsolutePath(), context.getResultBase());
        assertEquals(env, context.getEnv());
        assertEquals(Path.of(newWorkspace), this.workspace.path());
    }

    @Test
    void contextReload_envが空ならデフォルトのenvプロパティに戻る() {
        // Arrange
        this.workspace.contextReload(this.tempDir.toString(), "", "", "env/staging.properties");

        // Act
        this.workspace.contextReload(this.tempDir.toString(), "", "", "");

        // Assert
        assertNull(System.getProperty(FileResources.PROPERTY_ENV));
        assertEquals("", this.workspace.toDto().getContext().getEnv());
        assertEquals(new File(this.tempDir.toString(), FileResources.DEFAULT_ENV_PATH), FileResources.envFile());
    }

    @Test
    void toDto_全ての要素が正しく設定される() {
        // Act
        final WorkspaceDto dto = this.workspace.toDto();

        // Assert
        assertNotNull(dto.getParameterList());
        assertNotNull(dto.getResources());
        assertNotNull(dto.getContext());

        final var context = dto.getContext();
        assertEquals(this.tempDir.toAbsolutePath().normalize().toString(), context.getWorkspace());
        assertEquals(this.tempDir.toAbsolutePath().normalize().toString(), context.getDatasetBase());
        assertEquals(this.tempDir.toAbsolutePath().normalize().toString(), context.getResultBase());
        assertEquals(new File(this.tempDir.toString(), "resources/setting").toPath().toAbsolutePath().normalize().toString(), context.getSettingBase());
        assertEquals(new File(this.tempDir.toString(), "resources/template").toPath().toAbsolutePath().normalize().toString(), context.getTemplateBase());
        assertEquals(new File(this.tempDir.toString(), "resources/jdbc").toPath().toAbsolutePath().normalize().toString(), context.getJdbcBase());
        assertEquals(new File(this.tempDir.toString(), "resources/xlsx-schema").toPath().toAbsolutePath().normalize().toString(), context.getXlsxSchemaBase());
    }

    @Test
    void parameterNames_タイプに応じたパラメータ名が取得できる() {
        // Act
        final List<String> convertParams = this.workspace.parameterNames(Type.convert).toList();
        final List<String> compareParams = this.workspace.parameterNames(Type.compare).toList();

        // Assert
        assertNotNull(convertParams);
        assertNotNull(compareParams);
    }

    @Test
    void parameterFiles_タイプに応じたファイルが取得できる() {
        // Act
        final List<Path> convertFiles = this.workspace.parameterFiles(Type.convert).toList();
        final List<Path> compareFiles = this.workspace.parameterFiles(Type.compare).toList();

        // Assert
        assertNotNull(convertFiles);
        assertNotNull(compareFiles);
    }

    @Test
    void builder_パスが空の場合はカレントディレクトリが使用される() {
        // Act
        final Workspace.Builder builder = new Workspace.Builder();

        // Assert
        assertEquals(".", builder.getPath());
    }

    @Test
    void builder_指定されたパスでWorkspaceが生成される() {
        // Act
        final Workspace workspace = Workspace.builder()
                .setPath(this.tempDir.toString())
                .build();

        // Assert
        assertEquals(this.tempDir, workspace.path());
        assertNotNull(workspace.options());
        assertNotNull(workspace.resources());
    }

    @Test
    void parameterize_convertパラメータからparameterizeコマンドを生成する() throws IOException {
        // Arrange: convert パラメータを作成
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{}));

        // Act
        final String parameterizeName = this.workspace.parameterize(Type.convert, "myConvert", null, Workspace.ParamSource.DEFAULT);

        // Assert: 戻り値は parameterize コマンド名
        assertEquals("myConvert", parameterizeName);

        // Assert: parameterize コマンドファイルが作成される
        assertTrue(Files.exists(this.tempDir.resolve("option/parameterize/myConvert.txt")),
                "parameterize パラメータファイルが作成されること");

        // Assert: テンプレートファイルが作成される（name + ".txt" 形式）
        assertTrue(Files.exists(this.tempDir.resolve("option/parameterize/template/myConvert.txt")),
                "テンプレートファイルが作成されること");

        // Assert: パラメータソース CSV がワークスペース直下に作成される
        assertTrue(Files.exists(this.tempDir.resolve("myConvert.csv")),
                "パラメータソース CSV がワークスペース直下に作成されること");

        // Assert: 生成した parameterize コマンドが -cmd=convert を含む
        final CommandParameters loaded = this.workspace.options()
                .select(Type.parameterize, parameterizeName)
                .orElseThrow();
        final String content = loaded.content();
        assertTrue(content.contains("-cmd=convert"), "cmd=convert が設定されること");
        assertTrue(content.contains("-template=myConvert.txt"), "template=myConvert.txt が設定されること");
        assertTrue(content.contains("-param.src=myConvert.csv"), "param.src=myConvert.csv が設定されること");
        assertTrue(content.contains("-param.srcType=csv"), "param.srcType=csv が設定されること");
        assertTrue(content.contains("-unit=record"), "unit=record が設定されること");

        // Assert: テンプレートの各行はキーと値の間に = がある
        final String template = Files.readString(this.tempDir.resolve("option/parameterize/template/myConvert.txt"));
        assertTrue(template.contains("-src.srcType=$param.src_srcType$"), "template の各行が key=$param.xxx$ 形式で変数名に - を含まないこと");
        final String header = Files.readAllLines(this.tempDir.resolve("myConvert.csv")).getFirst();
        assertTrue(header.contains("src_srcType") && !header.contains("-src"), "CSVヘッダ名に - を含まないこと");
    }

    @Test
    void parameterize_存在しないパラメータを指定した場合は空文字を返す() throws IOException {
        final String result = this.workspace.parameterize(Type.convert, "nonexistent", null, Workspace.ParamSource.DEFAULT);
        assertEquals("", result);
    }

    @Test
    void parameterizeCandidates_値が入っているオプションをキーと値で返す() throws IOException {
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{"-src.src=in.csv"}));

        final List<ParameterizeCandidateDto> candidates = this.workspace.parameterizeCandidates(Type.convert, "myConvert");

        assertTrue(candidates.contains(new ParameterizeCandidateDto("-src.src", "in.csv")));
        assertTrue(candidates.stream().noneMatch(it -> it.value().isEmpty()), "値が空のオプションは含まないこと");
    }

    @Test
    void parameterize_選んだオプションだけパラメータ化し残りは値をテンプレートに書く() throws IOException {
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{"-src.src=in.csv"}));

        this.workspace.parameterize(Type.convert, "myConvert", List.of("-src.srcType"), Workspace.ParamSource.DEFAULT);

        final String template = Files.readString(this.tempDir.resolve("option/parameterize/template/myConvert.txt"));
        assertTrue(template.contains("-src.srcType=$param.src_srcType$"), "選んだオプションは $param.xxx$ になること");
        assertTrue(template.contains("-src.src=in.csv"), "選ばなかったオプションは保存値のままになること");
        assertEquals("src_srcType", Files.readAllLines(this.tempDir.resolve("myConvert.csv")).getFirst().replace("\"", ""));
    }

    @Test
    void parameterize_パラメータ化するオプションが無ければエラー() throws IOException {
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{}));

        assertThrows(IllegalArgumentException.class,
                () -> this.workspace.parameterize(Type.convert, "myConvert", List.of(), Workspace.ParamSource.DEFAULT));
    }

    @Test
    void parameterize_xlsxを選ぶとxlsxのパラメータデータを作りparameterizeで読める() throws IOException {
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{"-src.src=in.csv"}));

        this.workspace.parameterize(Type.convert, "myConvert", List.of("-src.src"), new Workspace.ParamSource("xlsx", "MS932"));

        assertTrue(Files.exists(this.tempDir.resolve("myConvert.xlsx")));
        final CommandParameters loaded = this.workspace.options().select(Type.parameterize, "myConvert").orElseThrow();
        assertTrue(loaded.content().contains("-param.srcType=xlsx"));
        assertFalse(loaded.content().contains("-param.encoding"), "xlsxでは文字コードを書かないこと");
        assertEquals(List.of(Map.of("src_src", "in.csv")), this.loadParams(loaded));
    }

    @Test
    void parameterize_csvは選んだ文字コードで書きparameterizeで読める() throws IOException {
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{"-src.src=入力.csv"}));

        this.workspace.parameterize(Type.convert, "myConvert", List.of("-src.src"), new Workspace.ParamSource("csv", "MS932"));

        final CommandParameters loaded = this.workspace.options().select(Type.parameterize, "myConvert").orElseThrow();
        assertTrue(loaded.content().contains("-param.encoding=MS932"));
        assertTrue(Files.readString(this.tempDir.resolve("myConvert.csv"), Charset.forName("MS932")).contains("入力.csv"));
        assertEquals(List.of(Map.of("src_src", "入力.csv")), this.loadParams(loaded));
    }

    @Test
    void ParamSource_未対応の形式や文字コードはエラー() {
        assertThrows(IllegalArgumentException.class, () -> new Workspace.ParamSource("xls", null));
        assertThrows(IllegalArgumentException.class, () -> new Workspace.ParamSource("csv", "no-such-encoding"));
    }

    @Test
    void parameterizeFiles_テンプレートとパラメータデータを返す() throws IOException {
        this.createParameterize();

        assertEquals(List.of(
                        new ParameterizeFileDto("-template", this.templatePath(), List.of()),
                        new ParameterizeFileDto("-param.src", this.paramPath(), List.of())),
                this.workspace.parameterizeFiles("myConvert"));
    }

    @Test
    void parameterizeFiles_他のparameterizeが参照するファイルはusedByに名前を返す() throws IOException {
        this.createParameterize();
        this.workspace.options().add("other", new CommandParameters(Type.parameterize,
                new String[]{"-cmd=convert", "-template=myConvert.txt", "-param.src=other.csv"}));

        assertEquals(List.of("other"), this.workspace.parameterizeFiles("myConvert").getFirst().usedBy());
        assertEquals(List.of(), this.workspace.parameterizeFiles("myConvert").getLast().usedBy());
    }

    @Test
    void deleteParameterize_指定したファイルも削除し候補一覧からも消す() throws IOException {
        this.createParameterize();

        this.workspace.deleteParameterize("myConvert", List.of(this.templatePath(), this.paramPath()));

        assertTrue(this.workspace.options().select(Type.parameterize, "myConvert").isEmpty());
        assertFalse(Files.exists(Path.of(this.templatePath())));
        assertFalse(Files.exists(Path.of(this.paramPath())));
        assertFalse(this.workspace.options().templates().list().contains("myConvert.txt"));
    }

    @Test
    void deleteParameterize_ファイルを指定しなければオプションだけ削除する() throws IOException {
        this.createParameterize();

        this.workspace.deleteParameterize("myConvert", List.of());

        assertTrue(this.workspace.options().select(Type.parameterize, "myConvert").isEmpty());
        assertTrue(Files.exists(Path.of(this.templatePath())));
        assertTrue(Files.exists(Path.of(this.paramPath())));
    }

    @Test
    void deleteParameterize_他から参照されるファイルや関係ないファイルを指定するとエラーで何も消さない() throws IOException {
        this.createParameterize();
        this.workspace.options().add("other", new CommandParameters(Type.parameterize,
                new String[]{"-cmd=convert", "-template=myConvert.txt", "-param.src=other.csv"}));
        final Path unrelated = Files.writeString(this.tempDir.resolve("unrelated.txt"), "");

        assertThrows(IllegalArgumentException.class,
                () -> this.workspace.deleteParameterize("myConvert", List.of(this.templatePath())));
        assertThrows(IllegalArgumentException.class,
                () -> this.workspace.deleteParameterize("myConvert", List.of(unrelated.toString())));

        assertTrue(this.workspace.options().select(Type.parameterize, "myConvert").isPresent());
        assertTrue(Files.exists(Path.of(this.templatePath())));
        assertTrue(Files.exists(unrelated));
    }

    private void createParameterize() throws IOException {
        this.workspace.options().add("myConvert", new CommandParameters(Type.convert, new String[]{"-src.src=in.csv"}));
        this.workspace.parameterize(Type.convert, "myConvert", List.of("-src.src"), Workspace.ParamSource.DEFAULT);
    }

    private String templatePath() {
        return this.tempDir.resolve("option/parameterize/template/myConvert.txt").toAbsolutePath().normalize().toString();
    }

    private String paramPath() {
        return this.tempDir.resolve("myConvert.csv").toAbsolutePath().normalize().toString();
    }

    private List<Map<String, Object>> loadParams(final CommandParameters parameterize) {
        try (final Stream<Parameter> params = new Parameterize().parseOption(parameterize.args()).loadParams()) {
            return params.map(param -> {
                final Map<String, Object> row = new HashMap<>(param.toMap());
                row.keySet().retainAll(Set.of("src_src"));
                return row;
            }).toList();
        }
    }
}
