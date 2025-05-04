package yo.dbunitcli.application.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.TestResourceLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;

/**
 * JSONファイルからXlsxSchemaを構築するビルダークラスのテスト
 */
public class FromJsonXlsxSchemaBuilderTest implements TestResourceLoader {

    private FromJsonXlsxSchemaBuilder builder;

    @BeforeEach
    protected void setUp() {
        builder = new FromJsonXlsxSchemaBuilder();
    }

    @Test
    @DisplayName("buildメソッドはnullを指定した場合はデフォルトスキーマを返すこと")
    public void buildWhenNullSchemaShouldReturnDefault() throws Exception {
        // 準備
        File nullFile = null;

        // 実行
        XlsxSchema schema = builder.build(nullFile);

        // 検証
        assertEquals(XlsxSchema.DEFAULT, schema);
    }

    @Test
    @DisplayName("buildメソッドは空文字を指定した場合はデフォルトスキーマを返すこと")
    public void buildWhenEmptyStringShouldReturnDefault() {
        // 準備
        String emptyJson = "";

        // 実行
        XlsxSchema schema = builder.build(emptyJson);

        // 検証
        assertEquals(XlsxSchema.DEFAULT, schema);
    }

    @Test
    @DisplayName("buildメソッドは行定義を含むJSONファイルからスキーマを構築できること")
    public void buildWhenSimpleRowsJsonFileShouldCreateSchema() throws Exception {
        // 準備
        File schemaFile = getTestResourceFile("simple_rows.json");

        // 実行
        XlsxSchema schema = builder.build(schemaFile);
        XlsxSchema.SimpleImpl simpleSchema = (XlsxSchema.SimpleImpl) schema;

        // 検証
        assertNotNull(schema);
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet1"), "シートパターンが正しく読み込まれていること");
        assertEquals(1, simpleSchema.rowsTableDefMap().get("Sheet1").size(), "テーブル定義が1つ存在すること");
        assertEquals("Table1", simpleSchema.rowsTableDefMap().get("Sheet1").get(0).tableName(), "テーブル名が正しく設定されていること");
    }

    @Test
    @DisplayName("buildメソッドはセル定義を含むJSONファイルからスキーマを構築できること")
    public void buildWhenCellsOnlyJsonFileShouldCreateSchema() throws Exception {
        // 準備
        File schemaFile = getTestResourceFile("cells_only.json");

        // 実行
        XlsxSchema schema = builder.build(schemaFile);
        XlsxSchema.SimpleImpl simpleSchema = (XlsxSchema.SimpleImpl) schema;

        // 検証
        assertNotNull(schema);
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet1"), "シートパターンが正しく読み込まれていること");
        assertEquals(1, simpleSchema.cellsTableDefMap().get("Sheet1").size(), "セル定義が1つ存在すること");
        assertEquals("Table1", simpleSchema.cellsTableDefMap().get("Sheet1").get(0).tableName(), "テーブル名が正しく設定されていること");
        assertTrue(simpleSchema.cellsTableDefMap().get("Sheet1").get(0).addOptional(), "addFileInfoが正しく設定されていること");
    }

    @Test
    @DisplayName("buildメソッドは行とセル定義を含むJSONファイルからスキーマを構築できること")
    public void buildWhenRowsAndCellsJsonFileShouldCreateSchema() throws Exception {
        // 準備
        File schemaFile = getTestResourceFile("rows_and_cells.json");

        // 実行
        XlsxSchema schema = builder.build(schemaFile);
        XlsxSchema.SimpleImpl simpleSchema = (XlsxSchema.SimpleImpl) schema;

        // 検証
        assertNotNull(schema);
        assertEquals(2, simpleSchema.sheetPatterns().size(), "2つのシートパターンが存在すること");
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet1"), "Sheet1のパターンが存在すること");
        assertTrue(simpleSchema.sheetPatterns().containsKey("Sheet2"), "Sheet2のパターンが存在すること");
        
        assertEquals(1, simpleSchema.rowsTableDefMap().get("Sheet1").size(), "Sheet1の行定義が1つ存在すること");
        assertEquals("Table1", simpleSchema.rowsTableDefMap().get("Sheet1").get(0).tableName(), "Sheet1のテーブル名が正しく設定されていること");
        assertTrue(simpleSchema.rowsTableDefMap().get("Sheet1").get(0).addOptional(), "Sheet1のaddFileInfoが正しく設定されていること");
        
        assertEquals(1, simpleSchema.cellsTableDefMap().get("Sheet2").size(), "Sheet2のセル定義が1つ存在すること");
        assertEquals("Table2", simpleSchema.cellsTableDefMap().get("Sheet2").get(0).tableName(), "Sheet2のテーブル名が正しく設定されていること");
    }

}