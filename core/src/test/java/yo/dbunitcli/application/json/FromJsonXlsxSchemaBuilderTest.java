package yo.dbunitcli.application.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.TestResourceLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSONファイルからXlsxSchemaを構築するビルダークラスのテスト
 */
public class FromJsonXlsxSchemaBuilderTest implements TestResourceLoader {

    private FromJsonXlsxSchemaBuilder builder;

    @BeforeEach
    protected void setUp() {
        this.builder = new FromJsonXlsxSchemaBuilder();
    }

    @Test
    @DisplayName("buildメソッドはnullを指定した場合はデフォルトスキーマを返すこと")
    public void buildWhenNullSchemaShouldReturnDefault() throws Exception {

        // 実行
        final XlsxSchema schema = this.builder.build((File) null);

        // 検証
        assertEquals(XlsxSchema.DEFAULT, schema);
    }

    @Test
    @DisplayName("buildメソッドは空文字を指定した場合はデフォルトスキーマを返すこと")
    public void buildWhenEmptyStringShouldReturnDefault() {
        // 準備
        final String emptyJson = "";

        // 実行
        final XlsxSchema schema = this.builder.build(emptyJson);

        // 検証
        assertEquals(XlsxSchema.DEFAULT, schema);
    }

    @Test
    @DisplayName("buildメソッドは行定義を含むJSONファイルからスキーマを構築できること")
    public void buildWhenSimpleRowsJsonFileShouldCreateSchema() throws Exception {
        // 準備
        final File schemaFile = this.getTestResourceFile("simple_rows.json");

        // 実行
        final XlsxSchema schema = this.builder.build(schemaFile);
        final XlsxSchema.SimpleImpl simpleSchema = (XlsxSchema.SimpleImpl) schema;

        // 検証
        assertNotNull(schema);
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet1"), "シートパターンが正しく読み込まれていること");
        assertEquals(1, simpleSchema.rowsTableDefMap().get("Sheet1").size(), "テーブル定義が1つ存在すること");
        assertEquals("Table1", simpleSchema.rowsTableDefMap().get("Sheet1").getFirst().tableName(), "テーブル名が正しく設定されていること");
    }

    @Test
    @DisplayName("buildメソッドはセル定義を含むJSONファイルからスキーマを構築できること")
    public void buildWhenCellsOnlyJsonFileShouldCreateSchema() throws Exception {
        // 準備
        final File schemaFile = this.getTestResourceFile("cells_only.json");

        // 実行
        final XlsxSchema schema = this.builder.build(schemaFile);
        final XlsxSchema.SimpleImpl simpleSchema = (XlsxSchema.SimpleImpl) schema;

        // 検証
        assertNotNull(schema);
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet1"), "シートパターンが正しく読み込まれていること");
        assertEquals(1, simpleSchema.cellsTableDefMap().get("Sheet1").size(), "セル定義が1つ存在すること");
        assertEquals("Table1", simpleSchema.cellsTableDefMap().get("Sheet1").getFirst().tableName(), "テーブル名が正しく設定されていること");
        assertTrue(simpleSchema.cellsTableDefMap().get("Sheet1").getFirst().addFileInfo(), "addFileInfoが正しく設定されていること");
    }

    @Test
    @DisplayName("buildメソッドは行とセル定義を含むJSONファイルからスキーマを構築できること")
    public void buildWhenRowsAndCellsJsonFileShouldCreateSchema() throws Exception {
        // 準備
        final File schemaFile = this.getTestResourceFile("rows_and_cells.json");

        // 実行
        final XlsxSchema schema = this.builder.build(schemaFile);
        final XlsxSchema.SimpleImpl simpleSchema = (XlsxSchema.SimpleImpl) schema;

        // 検証
        assertNotNull(schema);
        assertEquals(2, simpleSchema.sheetPatterns().size(), "2つのシートパターンが存在すること");
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet1"), "Sheet1のパターンが存在すること");
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet2"), "Sheet2のパターンが存在すること");

        assertEquals(1, simpleSchema.rowsTableDefMap().get("Sheet1").size(), "Sheet1の行定義が1つ存在すること");
        assertEquals("Table1", simpleSchema.rowsTableDefMap().get("Sheet1").getFirst().tableName(), "Sheet1のテーブル名が正しく設定されていること");
        assertTrue(simpleSchema.rowsTableDefMap().get("Sheet1").getFirst().addFileInfo(), "Sheet1のaddFileInfoが正しく設定されていること");

        assertEquals(1, simpleSchema.cellsTableDefMap().get("Sheet2").size(), "Sheet2のセル定義が1つ存在すること");
        assertEquals("Table2", simpleSchema.cellsTableDefMap().get("Sheet2").getFirst().tableName(), "Sheet2のテーブル名が正しく設定されていること");
    }

}