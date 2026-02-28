package yo.dbunitcli.sidecar.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptionsTest {

    @TempDir
    Path tempDir;

    private Options options;
    private File optionDir;

    @BeforeEach
    public void setUp() {
        final File workspace = this.tempDir.toFile();
        this.optionDir = new File(workspace, "option");
        final Options.Builder builder = Options.builder();
        builder.workspace(workspace);
        this.options = builder.build();
    }

    @Test
    public void builder_workspace_optionディレクトリが作成される() {
        assertTrue(this.optionDir.exists());
        assertTrue(this.optionDir.isDirectory());
    }

    @Test
    public void paths_ファイルが存在しない場合は空のStreamを返す() {
        for (final CommandType type : CommandType.values()) {
            final List<Path> result = this.options.paths(type).toList();
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void paths_addしたファイルのPathを返す() throws IOException {
        this.options.add("test", new CommandParameters(CommandType.compare, new String[]{"--arg=value"}));
        final List<Path> result = this.options.paths(CommandType.compare).toList();
        assertEquals(1, result.size());
        assertEquals("test.txt", result.getFirst().getFileName().toString());
    }

    @Test
    public void paths_コマンドタイプごとに独立したファイルセットを持つ() throws IOException {
        this.options.add("compareParam", new CommandParameters(CommandType.compare, new String[]{}));
        this.options.add("convertParam", new CommandParameters(CommandType.convert, new String[]{}));
        assertEquals(1, this.options.paths(CommandType.compare).toList().size());
        assertEquals(1, this.options.paths(CommandType.convert).toList().size());
    }

    @Test
    public void names_txtを除いた名前を返す() throws IOException {
        this.options.add("myParam", new CommandParameters(CommandType.convert, new String[]{}));
        final List<String> names = this.options.names(CommandType.convert).toList();
        assertEquals(List.of("myParam"), names);
    }

    @Test
    public void names_複数ファイルの名前リストを返す() throws IOException {
        this.options.add("alpha", new CommandParameters(CommandType.generate, new String[]{}));
        this.options.add("beta", new CommandParameters(CommandType.generate, new String[]{}));
        final List<String> names = this.options.names(CommandType.generate).toList();
        assertEquals(2, names.size());
        assertTrue(names.contains("alpha"));
        assertTrue(names.contains("beta"));
    }

    @Test
    public void add_txtファイルを作成する() throws IOException {
        this.options.add("newParam", new CommandParameters(CommandType.generate, new String[]{}));
        final File file = new File(new File(this.optionDir, "generate"), "newParam.txt");
        assertTrue(file.exists());
    }

    @Test
    public void add_argsをCRLFで連結してファイル内容とする() throws IOException {
        this.options.add("runParam", new CommandParameters(CommandType.run, new String[]{"--arg1=a", "--arg2=b"}));
        final File file = new File(new File(this.optionDir, "run"), "runParam.txt");
        assertEquals("--arg1=a\r\n--arg2=b", Files.readString(file.toPath()));
    }

    @Test
    public void add_同名ファイルが存在する場合は連番付き名前で追加する() throws IOException {
        this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        final List<String> names = this.options.names(CommandType.compare).toList();
        assertTrue(names.contains("dup"));
        assertTrue(names.contains("dup(1)"));
    }

    @Test
    public void add_連番1が存在する場合は連番2を使う() throws IOException {
        this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        final List<String> names = this.options.names(CommandType.compare).toList();
        assertTrue(names.contains("dup"));
        assertTrue(names.contains("dup(1)"));
        assertTrue(names.contains("dup(2)"));
    }

    @Test
    public void delete_パラメータファイルを削除する() throws IOException {
        this.options.add("toDelete", new CommandParameters(CommandType.compare, new String[]{}));
        this.options.delete(CommandType.compare, "toDelete");
        final List<String> names = this.options.names(CommandType.compare).toList();
        assertFalse(names.contains("toDelete"));
        final File file = new File(new File(this.optionDir, "compare"), "toDelete.txt");
        assertFalse(file.exists());
    }

    @Test
    public void delete_存在しないファイル名を指定した場合は例外をスローする() {
        assertThrows(IOException.class, () ->
                this.options.delete(CommandType.compare, "nonexistent")
        );
    }

    @Test
    public void rename_パラメータファイル名を変更する() throws IOException {
        this.options.add("oldName", new CommandParameters(CommandType.convert, new String[]{}));
        this.options.rename(CommandType.convert, "oldName", "newName");
        final List<String> names = this.options.names(CommandType.convert).toList();
        assertFalse(names.contains("oldName"));
        assertTrue(names.contains("newName"));
        assertFalse(new File(new File(this.optionDir, "convert"), "oldName.txt").exists());
        assertTrue(new File(new File(this.optionDir, "convert"), "newName.txt").exists());
    }

    @Test
    public void rename_存在しないファイルの場合は何もしない() {
        assertDoesNotThrow(() ->
                                   this.options.rename(CommandType.compare, "nonexistent", "newName")
        );
    }

    @Test
    public void update_指定した名前でファイルを作成する() throws IOException {
        this.options.update("test", new CommandParameters(CommandType.compare, new String[]{"--key=value"}));
        final File file = new File(new File(this.optionDir, "compare"), "test.txt");
        assertTrue(file.exists());
        assertEquals("--key=value", Files.readString(file.toPath()));
    }

    @Test
    public void update_既存ファイルがあれば上書きする() throws IOException {
        this.options.update("test", new CommandParameters(CommandType.compare, new String[]{"--key=value"}));
        this.options.update("test", new CommandParameters(CommandType.compare, new String[]{"--key=value2"}));
        final File file = new File(new File(this.optionDir, "compare"), "test.txt");
        assertTrue(file.exists());
        assertEquals("--key=value2", Files.readString(file.toPath()));
    }

    @Test
    public void update_argsをCRLFで連結してファイル内容とする() throws IOException {
        this.options.update("myParam", new CommandParameters(CommandType.run, new String[]{"--a=1", "--b=2"}));
        final File file = new File(new File(this.optionDir, "run"), "myParam.txt");
        assertEquals("--a=1\r\n--b=2", Files.readString(file.toPath()));
    }

    @Test
    public void builder_workspace_全コマンドタイプが登録されnamesが呼び出せる() {
        for (final CommandType type : CommandType.values()) {
            assertDoesNotThrow(() -> this.options.names(type).toList(),
                    type.name() + " の names() が例外をスロー");
        }
    }

    @Test
    public void builder_workspace_templatesがparameterizeのtemplateサブディレクトリに設定される() {
        final File expectedTemplateDir = new File(new File(this.optionDir, "parameterize"), "template");
        // templatesのbaseDirが期待のパスになっていることを確認するため、templateディレクトリが作成できることを確認
        assertTrue(this.optionDir.exists());
        assertEquals(expectedTemplateDir.getAbsolutePath(), this.options.templates().baseDir().getAbsolutePath());
    }

    @Test
    public void add_戻り値はtxtを除いたファイル名を返す() throws IOException {
        final String result = this.options.add("myParam", new CommandParameters(CommandType.compare, new String[]{}));
        assertEquals("myParam", result);
    }

    @Test
    public void add_同名ファイルが存在する場合の戻り値は連番付き名前を返す() throws IOException {
        this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        final String result = this.options.add("dup", new CommandParameters(CommandType.compare, new String[]{}));
        assertEquals("dup(1)", result);
    }

    @Test
    public void select_存在するファイルを指定するとParametersを返す() throws IOException {
        this.options.add("selected", new CommandParameters(CommandType.compare, new String[]{"--key=val"}));
        final var result = this.options.select(CommandType.compare, "selected");
        assertTrue(result.isPresent());
        assertEquals(CommandType.compare, result.get().type());
    }

    @Test
    public void select_存在しないファイルを指定するとemptyを返す() {
        final var result = this.options.select(CommandType.compare, "nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    public void select_ファイルの内容がargsとして読み込まれる() throws IOException {
        this.options.add("loadMe", new CommandParameters(CommandType.run, new String[]{"--a=1", "--b=2"}));
        final var result = this.options.select(CommandType.run, "loadMe");
        assertTrue(result.isPresent());
        final String[] args = result.get().args();
        assertEquals(2, args.length);
        assertEquals("--a=1", args[0]);
        assertEquals("--b=2", args[1]);
    }

    @Test
    public void newItem_デフォルト名でパラメータファイルが作成される() throws IOException {
        this.options.newItem(CommandType.compare);
        final List<String> names = this.options.names(CommandType.compare).toList();
        assertTrue(names.contains("new item"));
        final File file = new File(new File(this.optionDir, "compare"), "new item.txt");
        assertTrue(file.exists());
    }

    @Test
    public void newItem_複数回呼ぶと連番付きで作成される() throws IOException {
        this.options.newItem(CommandType.generate);
        this.options.newItem(CommandType.generate);
        final List<String> names = this.options.names(CommandType.generate).toList();
        assertTrue(names.contains("new item"));
        assertTrue(names.contains("new item(1)"));
    }

    @Test
    public void copy_指定したファイルのコピーを連番付き名前で作成する() throws IOException {
        this.options.add("original", new CommandParameters(CommandType.convert, new String[]{"--key=value"}));
        this.options.copy(CommandType.convert, "original");
        final List<String> names = this.options.names(CommandType.convert).toList();
        assertTrue(names.contains("original"));
        assertTrue(names.contains("original(1)"));
    }

    @Test
    public void copy_コピーしたファイルは元ファイルと同じ内容を持つ() throws IOException {
        this.options.add("source", new CommandParameters(CommandType.run, new String[]{"--arg=abc"}));
        this.options.copy(CommandType.run, "source");
        final File copied = new File(new File(this.optionDir, "run"), "source(1).txt");
        assertTrue(copied.exists());
        assertEquals("--arg=abc", Files.readString(copied.toPath()));
    }

    @Test
    public void copy_存在しないファイルを指定した場合は何もしない() {
        assertDoesNotThrow(() -> this.options.copy(CommandType.compare, "nonexistent"));
        final List<String> names = this.options.names(CommandType.compare).toList();
        assertTrue(names.isEmpty());
    }

}
