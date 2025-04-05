package yo.dbunitcli.sidecar.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
        this.resourceFile = new ResourceFile(this.parentDir);
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
        this.resourceFile = new ResourceFile(this.parentDir);

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
        this.resourceFile = new ResourceFile(this.parentDir);

        final String content = this.resourceFile.read("test.json").get();
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
        this.resourceFile = new ResourceFile(this.parentDir);

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
        this.resourceFile = new ResourceFile(this.parentDir);

        final Path filePath = this.tempDir.resolve("test.json");
        assertTrue(Files.exists(filePath));

        this.resourceFile.delete("test.json");
        assertFalse(Files.exists(filePath));
    }

    private void createTestFile(final String filename, final String content) throws IOException {
        final Path filePath = this.tempDir.resolve(filename);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }
}