package yo.dbunitcli.sidecar.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.resource.FileResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceFileTest {

    @TempDir
    Path tempDir;

    private ResourceFile resourceFile;
    private File parentDir;

    @BeforeEach
    public void setUp() {
        this.parentDir = this.tempDir.toFile();
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);
    }

    @Test
    public void list_空のディレクトリの場合は空のリストを返す() {
        final List<String> files = this.resourceFile.list();
        assertTrue(files.isEmpty());
    }

    @Test
    public void list_ファイルが存在する場合はそのリストを返す() throws IOException {
        // テストファイルを作成
        this.createTestFile("test1.json", "{}");
        this.createTestFile("test2.json", "{}");

        // 新しいResourceFileインスタンスを作成（ファイルをスキャンするため）
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        final List<String> files = this.resourceFile.list();
        assertEquals(2, files.size());
        assertTrue(files.contains("test1.json"));
        assertTrue(files.contains("test2.json"));
    }

    @Test
    public void read_ファイルが存在する場合はその内容を返す() throws IOException {
        final String expectedContent = "{\"key\": \"value\"}";
        this.createTestFile("test.json", expectedContent);

        // 新しいResourceFileインスタンスを作成
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        final String content = this.resourceFile.read("test.json", "").orElseThrow();
        assertEquals(expectedContent, content);
    }

    @Test
    public void update_新規ファイルの場合はファイルを作成する() throws IOException {
        final String content = "{\"key\": \"value\"}";
        this.resourceFile.update("new.json", content);

        final Path filePath = this.tempDir.resolve("new.json");
        assertTrue(Files.exists(filePath));
        assertEquals(content, Files.readString(filePath));
    }

    @Test
    public void update_既存ファイルの場合は上書きする() throws IOException {
        final String initialContent = "{\"key\": \"initial\"}";
        final String updatedContent = "{\"key\": \"updated\"}";

        this.createTestFile("test.json", initialContent);
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        this.resourceFile.update("test.json", updatedContent);

        final Path filePath = this.tempDir.resolve("test.json");
        assertEquals(updatedContent, Files.readString(filePath));
    }

    @Test
    public void delete_存在しないファイルの場合は例外をスローする() {
        assertThrows(IOException.class, () ->
                this.resourceFile.delete("nonexistent.json")
        );
    }

    @Test
    public void delete_ファイルが存在する場合は削除する() throws IOException {
        this.createTestFile("test.json", "{}");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        final Path filePath = this.tempDir.resolve("test.json");
        assertTrue(Files.exists(filePath));

        this.resourceFile.delete("test.json");
        assertFalse(Files.exists(filePath));
    }

    @Test
    public void read_ファイルが存在しない場合はemptyを返す() {
        final var result = this.resourceFile.read("nonexistent.json", "");
        assertTrue(result.isEmpty());
    }

    @Test
    public void select_ファイルが存在する場合はPathを返す() throws IOException {
        this.createTestFile("test.json", "{}");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        final var result = this.resourceFile.select("test.json");
        assertTrue(result.isPresent());
        assertEquals(this.tempDir.resolve("test.json"), result.get());
    }

    @Test
    public void select_ファイルが存在しない場合はemptyを返す() {
        final var result = this.resourceFile.select("nonexistent.json");
        assertTrue(result.isEmpty());
    }

    @Test
    public void add_新規ファイルを追加する() throws IOException {
        final String content = "{\"key\": \"value\"}";
        this.resourceFile.add("new.json", content);

        final Path filePath = this.tempDir.resolve("new.json");
        assertTrue(Files.exists(filePath));
        assertEquals(content, Files.readString(filePath));
        assertTrue(this.resourceFile.list().contains("new.json"));
    }

    @Test
    public void add_同名ファイルが存在する場合は連番付き名前で追加する() throws IOException {
        this.createTestFile("test.json", "{}");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        this.resourceFile.add("test.json", "{\"key\": \"1\"}");

        final List<String> files = this.resourceFile.list();
        assertTrue(files.contains("test.json"));
        assertTrue(files.contains("test(1).json"));
        assertTrue(Files.exists(this.tempDir.resolve("test(1).json")));
    }

    @Test
    public void add_連番1が存在する場合は連番2を使う() throws IOException {
        this.createTestFile("test.json", "{}");
        this.createTestFile("test(1).json", "{}");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        this.resourceFile.add("test.json", "{\"key\": \"2\"}");

        final List<String> files = this.resourceFile.list();
        assertTrue(files.contains("test(2).json"));
        assertTrue(Files.exists(this.tempDir.resolve("test(2).json")));
    }

    @Test
    public void rename_ファイル名を変更する() throws IOException {
        this.createTestFile("old.json", "{\"key\": \"value\"}");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        this.resourceFile.rename("old.json", "new.json");

        assertFalse(Files.exists(this.tempDir.resolve("old.json")));
        assertTrue(Files.exists(this.tempDir.resolve("new.json")));
        final List<String> files = this.resourceFile.list();
        assertFalse(files.contains("old.json"));
        assertTrue(files.contains("new.json"));
    }

    @Test
    public void rename_存在しないファイルの場合は何もしない() {
        assertDoesNotThrow(() -> this.resourceFile.rename("nonexistent.json", "new.json"));
        assertTrue(this.resourceFile.list().isEmpty());
    }

    @Test
    public void copy_指定したソースの内容をデスティネーションにコピーする() throws IOException {
        this.createTestFile("source.txt", "hello");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        this.resourceFile.copy("source.txt");

        assertTrue(Files.exists(this.tempDir.resolve("source(1).txt")));
        assertEquals("hello", Files.readString(this.tempDir.resolve("source(1).txt"), StandardCharsets.UTF_8));
        assertTrue(this.resourceFile.list().contains("source(1).txt"));
    }

    @Test
    public void copy_ソースが存在しない場合は何もしない() {
        assertDoesNotThrow(() -> this.resourceFile.copy("nonexistent.txt"));
        assertFalse(Files.exists(this.tempDir.resolve("nonexistent(1).txt")));
    }

    @Test
    public void constructor_存在しないディレクトリでも例外をスローしない() {
        final File nonExistent = new File(this.tempDir.toFile(), "does_not_exist");
        assertDoesNotThrow(() -> new ResourceFile(nonExistent, FileResources::searchWorkspace, StandardCharsets.UTF_8));
    }

    @Test
    public void ディレクトリはファイルとして扱わない() throws IOException {
        Files.createDirectories(this.tempDir.resolve("dir"));

        assertTrue(this.resourceFile.read("dir", "").isEmpty());
        assertTrue(this.resourceFile.locate("dir").isEmpty());
        assertThrows(IOException.class, () -> this.resourceFile.save("dir", "x", "", true));
    }

    @Test
    public void save_overwriteなら探索で見つかったbaseDir外のファイルへ書く() throws IOException {
        final ResourceFile withFallback = this.withFallbackDir();
        final Path other = this.tempDir.resolve("other/shared.sql");
        Files.createDirectories(other.getParent());
        Files.writeString(other, "old");

        withFallback.save("shared.sql", "new", "", true);

        assertEquals("new", Files.readString(other));
        assertFalse(Files.exists(this.baseDir().resolve("shared.sql")), "baseDirには作らない");
    }

    @Test
    public void save_overwriteでなければbaseDirへ新規保存する() throws IOException {
        final ResourceFile withFallback = this.withFallbackDir();
        final Path other = this.tempDir.resolve("other/shared.sql");
        Files.createDirectories(other.getParent());
        Files.writeString(other, "old");

        withFallback.save("shared.sql", "new", "", false);

        assertEquals("old", Files.readString(other));
        assertEquals("new", Files.readString(this.baseDir().resolve("shared.sql")));
        assertTrue(withFallback.list().contains("shared.sql"));
    }

    @Test
    public void save_overwriteで見つからなければ例外() {
        assertThrows(IOException.class, () -> this.withFallbackDir().save("missing.sql", "new", "", true));
    }

    @Test
    public void save_サブディレクトリへの新規保存は一覧に載せない() throws IOException {
        this.resourceFile.save("sub/new.json", "{}", "", false);

        assertTrue(Files.exists(this.tempDir.resolve("sub/new.json")));
        assertFalse(this.resourceFile.list().contains("new.json"), "baseDir直下に無い名前を一覧に出さない");
    }

    @Test
    public void locate_baseDir外で見つかったファイルの場所と新規保存先を返す() throws IOException {
        final ResourceFile withFallback = this.withFallbackDir();
        final Path other = this.tempDir.resolve("other/shared.sql");
        Files.createDirectories(other.getParent());
        Files.writeString(other, "old");

        final ResourceFile.Location location = withFallback.locate("shared.sql").orElseThrow();

        assertEquals(other.getParent().toAbsolutePath().normalize().toString(), location.folder());
        assertEquals(this.baseDir().toAbsolutePath().normalize().toString(), location.defaultFolder());
        assertEquals("shared.sql", location.fileName());
        assertFalse(location.defaultFolderHasFile());
    }

    @Test
    public void locate_baseDir直下のファイルはフォルダがdefaultFolderと一致する() throws IOException {
        this.createTestFile("test.json", "{}");
        this.resourceFile = new ResourceFile(this.parentDir, FileResources::searchWorkspace, StandardCharsets.UTF_8);

        final ResourceFile.Location location = this.resourceFile.locate("test.json").orElseThrow();

        assertEquals(location.defaultFolder(), location.folder());
        assertTrue(location.defaultFolderHasFile());
    }

    @Test
    public void locate_見つからない名前と空の名前はempty() {
        assertTrue(this.resourceFile.locate("missing.json").isEmpty());
        assertTrue(this.resourceFile.locate("").isEmpty());
    }

    // baseDir（tempDir/base）に無ければtempDir/otherを探すResourceFile
    private ResourceFile withFallbackDir() throws IOException {
        Files.createDirectories(this.baseDir());
        return new ResourceFile(this.baseDir().toFile(),
                name -> new File(this.tempDir.toFile(), "other/" + name), StandardCharsets.UTF_8);
    }

    private Path baseDir() {
        return this.tempDir.resolve("base");
    }

    private void createTestFile(final String filename, final String content) throws IOException {
        final Path filePath = this.tempDir.resolve(filename);
        Files.writeString(filePath, content);
    }
}