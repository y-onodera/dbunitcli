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
    void setUp() {
        parentDir = tempDir.toFile();
        resourceFile = new ResourceFile(parentDir);
    }

    @Test
    void list_空のディレクトリの場合は空のリストを返す() {
        List<String> files = resourceFile.list();
        assertTrue(files.isEmpty());
    }

    @Test
    void list_ファイルが存在する場合はそのリストを返す() throws IOException {
        // テストファイルを作成
        createTestFile("test1.json", "{}");
        createTestFile("test2.json", "{}");

        // 新しいResourceFileインスタンスを作成（ファイルをスキャンするため）
        resourceFile = new ResourceFile(parentDir);

        List<String> files = resourceFile.list();
        assertEquals(2, files.size());
        assertTrue(files.contains("test1.json"));
        assertTrue(files.contains("test2.json"));
    }

    @Test
    void read_存在しないファイルの場合は空のJSONを返す() {
        String content = resourceFile.read("nonexistent.json");
        assertEquals("{}", content);
    }

    @Test
    void read_ファイルが存在する場合はその内容を返す() throws IOException {
        String expectedContent = "{\"key\": \"value\"}";
        createTestFile("test.json", expectedContent);

        // 新しいResourceFileインスタンスを作成
        resourceFile = new ResourceFile(parentDir);

        String content = resourceFile.read("test.json");
        assertEquals(expectedContent, content);
    }

    @Test
    void update_新規ファイルの場合はファイルを作成する() throws IOException {
        String content = "{\"key\": \"value\"}";
        resourceFile.update("new.json", content);

        Path filePath = tempDir.resolve("new.json");
        assertTrue(Files.exists(filePath));
        assertEquals(content, Files.readString(filePath));
    }

    @Test
    void update_既存ファイルの場合は上書きする() throws IOException {
        String initialContent = "{\"key\": \"initial\"}";
        String updatedContent = "{\"key\": \"updated\"}";
        
        createTestFile("test.json", initialContent);
        resourceFile = new ResourceFile(parentDir);
        
        resourceFile.update("test.json", updatedContent);

        Path filePath = tempDir.resolve("test.json");
        assertEquals(updatedContent, Files.readString(filePath));
    }

    @Test
    void delete_存在しないファイルの場合は例外をスローする() {
        assertThrows(IOException.class, () -> 
            resourceFile.delete("nonexistent.json")
        );
    }

    @Test
    void delete_ファイルが存在する場合は削除する() throws IOException {
        createTestFile("test.json", "{}");
        resourceFile = new ResourceFile(parentDir);

        Path filePath = tempDir.resolve("test.json");
        assertTrue(Files.exists(filePath));

        resourceFile.delete("test.json");
        assertFalse(Files.exists(filePath));
    }

    private void createTestFile(String filename, String content) throws IOException {
        Path filePath = tempDir.resolve(filename);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }
}