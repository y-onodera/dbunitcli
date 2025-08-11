package yo.dbunitcli.sidecar.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkspaceTest {
    @TempDir
    private Path tempDir;

    private Workspace workspace;

    @BeforeEach
    void setUp() {
        System.clearProperty(FileResources.PROPERTY_WORKSPACE);
        System.clearProperty(FileResources.PROPERTY_DATASET_BASE);
        System.clearProperty(FileResources.PROPERTY_RESULT_BASE);
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

        // Act
        this.workspace.contextReload(newWorkspace, datasetBase, resultBase);

        // Assert
        final WorkspaceDto dto = this.workspace.toDto();
        final var context = dto.getContext();
        assertEquals(newWorkspace, context.getWorkspace());
        assertEquals(new File(datasetBase).getAbsolutePath(), context.getDatasetBase());
        assertEquals(new File(resultBase).getAbsolutePath(), context.getResultBase());
        assertEquals(Path.of(newWorkspace), this.workspace.path());
    }

    @Test
    void toDto_全ての要素が正しく設定される() {
        // Act
        final WorkspaceDto dto = this.workspace.toDto();

        // Assert
        assertNotNull(dto.getParameterList());
        assertNotNull(dto.getResources());
        assertNotNull(dto.getContext());
        assertNotNull(dto.getDatasourceFiles());

        final var context = dto.getContext();
        assertEquals(this.tempDir.toAbsolutePath().normalize().toString(), context.getWorkspace());
        assertEquals(this.tempDir.toAbsolutePath().normalize().toString(), context.getDatasetBase());
        assertEquals(this.tempDir.toAbsolutePath().normalize().toString(), context.getResultBase());
        assertEquals(new File(this.tempDir.toString(), "resources/setting").toPath().toAbsolutePath().normalize().toString(), context.getSettingBase());
        assertEquals(new File(this.tempDir.toString(), "resources/template").toPath().toAbsolutePath().normalize().toString(), context.getTemplateBase());
        assertEquals(new File(this.tempDir.toString(), "resources/jdbc").toPath().toAbsolutePath().normalize().toString(), context.getJdbcBase());
        assertEquals(new File(this.tempDir.toString(), "resources/xlsxSchema").toPath().toAbsolutePath().normalize().toString(), context.getXlsxSchemaBase());
    }

    @Test
    void parameterNames_タイプに応じたパラメータ名が取得できる() {
        // Act
        final List<String> convertParams = this.workspace.parameterNames(CommandType.convert).toList();
        final List<String> compareParams = this.workspace.parameterNames(CommandType.compare).toList();

        // Assert
        assertNotNull(convertParams);
        assertNotNull(compareParams);
    }

    @Test
    void parameterFiles_タイプに応じたファイルが取得できる() {
        // Act
        final List<Path> convertFiles = this.workspace.parameterFiles(CommandType.convert).toList();
        final List<Path> compareFiles = this.workspace.parameterFiles(CommandType.compare).toList();

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
}