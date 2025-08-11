package yo.dbunitcli.sidecar.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.WorkspaceDto;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceTest {
    @TempDir
    Path tempDir;
    
    private Workspace workspace;
    
    @BeforeEach
    void setUp() {
        System.clearProperty(FileResources.PROPERTY_WORKSPACE);
        System.clearProperty(FileResources.PROPERTY_DATASET_BASE);
        System.clearProperty(FileResources.PROPERTY_RESULT_BASE);
        workspace = Workspace.builder()
                .setPath(tempDir.toString())
                .build();
    }
    
    @Test
    void contextReload_プロパティが正しく設定される() {
        // Arrange
        String newWorkspace = tempDir.resolve("new").toString();
        String datasetBase = "dataset";
        String resultBase = "result";
        
        // Act
        workspace.contextReload(newWorkspace, datasetBase, resultBase);
        
        // Assert
        WorkspaceDto dto = workspace.toDto();
        var context = dto.getContext();
        assertEquals(newWorkspace, context.getWorkspace());
        assertEquals(new File(datasetBase).getAbsolutePath(), context.getDatasetBase());
        assertEquals(new File(resultBase).getAbsolutePath(), context.getResultBase());
        assertEquals(Path.of(newWorkspace), workspace.path());
    }
    
    @Test
    void toDto_全ての要素が正しく設定される() {
        // Act
        WorkspaceDto dto = workspace.toDto();
        
        // Assert
        assertNotNull(dto.getParameterList());
        assertNotNull(dto.getResources());
        assertNotNull(dto.getContext());
        assertNotNull(dto.getDatasourceFiles());
        
        var context = dto.getContext();
        assertEquals(tempDir.toAbsolutePath().normalize().toString(),context.getWorkspace());
        assertEquals(tempDir.toAbsolutePath().normalize().toString(),context.getDatasetBase());
        assertEquals(tempDir.toAbsolutePath().normalize().toString(),context.getResultBase());
        assertEquals(new File(tempDir.toString(),"resources/setting").toPath().toAbsolutePath().normalize().toString(),context.getSettingBase());
        assertEquals(new File(tempDir.toString(),"resources/template").toPath().toAbsolutePath().normalize().toString(),context.getTemplateBase());
        assertEquals(new File(tempDir.toString(),"resources/jdbc").toPath().toAbsolutePath().normalize().toString(),context.getJdbcBase());
        assertEquals(new File(tempDir.toString(),"resources/xlsxSchema").toPath().toAbsolutePath().normalize().toString(),context.getXlsxSchemaBase());
    }
    
    @Test
    void parameterNames_タイプに応じたパラメータ名が取得できる() {
        // Act
        List<String> convertParams = workspace.parameterNames(CommandType.convert).toList();
        List<String> compareParams = workspace.parameterNames(CommandType.compare).toList();
        
        // Assert
        assertNotNull(convertParams);
        assertNotNull(compareParams);
    }
    
    @Test
    void parameterFiles_タイプに応じたファイルが取得できる() {
        // Act
        List<Path> convertFiles = workspace.parameterFiles(CommandType.convert).toList();
        List<Path> compareFiles = workspace.parameterFiles(CommandType.compare).toList();
        
        // Assert
        assertNotNull(convertFiles);
        assertNotNull(compareFiles);
    }
    
    @Test
    void builder_パスが空の場合はカレントディレクトリが使用される() {
        // Act
        Workspace.Builder builder = new Workspace.Builder();
        
        // Assert
        assertEquals(".", builder.getPath());
    }
    
    @Test
    void builder_指定されたパスでWorkspaceが生成される() {
        // Act
        Workspace workspace = Workspace.builder()
                .setPath(tempDir.toString())
                .build();
        
        // Assert
        assertEquals(tempDir, workspace.path());
        assertNotNull(workspace.options());
        assertNotNull(workspace.resources());
    }
}