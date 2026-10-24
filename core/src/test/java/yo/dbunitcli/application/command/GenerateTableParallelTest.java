package yo.dbunitcli.application.command;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMappingTask;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * unit=tableのテンプレート出力をテーブルごとに並列で行っても、逐次と同じ結果になること
 */
public class GenerateTableParallelTest {

    private static final List<String> TABLES = List.of("a", "b", "c", "d");

    @TempDir
    Path tempDir;

    private File src;

    private File template;

    @BeforeEach
    public void setUp() throws IOException {
        this.src = Files.createDirectories(this.tempDir.resolve("src")).toFile();
        for (final String name : GenerateTableParallelTest.TABLES) {
            final String body = IntStream.range(0, 100)
                    .mapToObj(i -> "\"" + name + i + "\",\"" + i + "\"")
                    .collect(Collectors.joining("\n", "\"ID\",\"VAL\"\n", "\n"));
            Files.writeString(new File(this.src, name + ".csv").toPath(), body, StandardCharsets.UTF_8);
        }
        this.template = this.tempDir.resolve("template.txt").toFile();
        Files.writeString(this.template.toPath(), "$tableName$:$rows:{it|$it.ID$};separator=\",\"$", StandardCharsets.UTF_8);
    }

    @Test
    public void generate_txt_per_table() throws IOException {
        final File result = this.tempDir.resolve("result").toFile();
        Generate.main(this.txtArgs(result, "$tableName$.txt"));
        for (final String name : GenerateTableParallelTest.TABLES) {
            Assertions.assertEquals(this.expected(name)
                    , Files.readString(new File(result, name + ".txt").toPath(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void generate_txt_to_same_file_keeps_last_table() throws IOException {
        // 全テーブルが同じファイルへ出力する場合は逐次で処理し、最後のテーブルの内容が残る
        final File result = this.tempDir.resolve("result").toFile();
        Generate.main(this.txtArgs(result, "all.txt"));
        Assertions.assertEquals(this.expected("d")
                , Files.readString(new File(result, "all.txt").toPath(), StandardCharsets.UTF_8));
    }

    @Test
    public void generate_sql_from_db_with_connection_per_worker() throws SQLException, IOException {
        final String url = "jdbc:h2:mem:generateTableParallel;DB_CLOSE_DELAY=-1";
        final File listFile = this.tempDir.resolve("tables.txt").toFile();
        try (final Connection conn = DriverManager.getConnection(url, "sa", "sa");
             final Statement stmt = conn.createStatement()) {
            for (final String table : List.of("T1", "T2", "T3", "T4")) {
                stmt.execute("create table " + table + " (ID varchar(10) primary key, VAL varchar(10))");
                stmt.execute("insert into " + table + " select '" + table + "-' || x, x from system_range(1, 100)");
            }
            Files.writeString(listFile.toPath(), String.join("\n", "T1", "T2", "T3", "T4"), StandardCharsets.UTF_8);
            final int before = this.count(stmt, "information_schema.sessions");
            final File result = this.tempDir.resolve("result").toFile();
            Generate.main(new String[]{
                    "-generateType=sql"
                    , "-op=INSERT"
                    , "-srcType=table"
                    , "-src=" + listFile.getAbsolutePath()
                    , "-jdbcUrl=" + url
                    , "-jdbcUser=sa"
                    , "-jdbcPass=sa"
                    , "-result=" + result.getAbsolutePath()
                    , "-resultPath=sql"
            });
            // 2つ目以降の作業スレッドで張った接続は処理終了時に閉じる（残るのはsrcを読む元の接続だけ。逐次でも同じく残る）
            Assertions.assertEquals(before + 1, this.count(stmt, "information_schema.sessions"));
            for (final String table : List.of("T1", "T2", "T3", "T4")) {
                final String sql = Files.readString(new File(result, "sql/" + table + ".sql").toPath(), StandardCharsets.UTF_8);
                for (int i = 1; i <= 100; i++) {
                    Assertions.assertTrue(sql.contains("'" + table + "-" + i + "'"), table + "-" + i);
                }
                List.of("T1", "T2", "T3", "T4").stream()
                        .filter(other -> !other.equals(table))
                        .forEach(other -> Assertions.assertFalse(sql.contains("'" + other + "-"), table + " contains " + other));
            }
        }
    }

    @Test
    public void load_does_not_keep_rows_in_unloaded_dto() {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                .setSrc(this.src)
                .setSource(DataSourceType.csv)
                .setEncoding("UTF-8")
                .build();
        final List<ParameterUnit.SeparatedTableTemplate> templates = ParameterUnit.tableTemplates(
                        new ComparableDataSetLoader(Parameter.none()), new ComparableCsvDataSetProducer(param), TableSeparators.NONE)
                .map(ParameterUnit.SeparatedTableTemplate.class::cast)
                .toList();
        Assertions.assertEquals(GenerateTableParallelTest.TABLES.size(), templates.size());
        for (final ParameterUnit.SeparatedTableTemplate template : templates) {
            final Parameter loaded = template.load();
            Assertions.assertEquals(100, ((List<?>) loaded.get("rows")).size());
            // 読込前のDTOは行を持たない（処理済みテーブルの行が一覧に残らない）
            Assertions.assertInstanceOf(ComparableTableMappingTask.WithTargetTable.class, template.dto().getRows());
        }
    }

    @ParameterizedTest
    @CsvSource({"xlsx,true", "xlsx,false", "xls,true", "xls,false"})
    public void generate_excel_per_table(final String extension, final boolean lazyLoad) throws IOException {
        final File template = this.excelTemplate(extension);
        for (final String parallel : List.of("", "1")) {
            final File result = this.tempDir.resolve("result-" + extension + lazyLoad + parallel).toFile();
            Generate.main(new String[]{
                    "-srcType=csv"
                    , "-src=" + this.src.getAbsolutePath()
                    , "-encoding=UTF-8"
                    , "-generateType=" + extension
                    , "-unit=table"
                    , "-lazyLoad=" + lazyLoad
                    , "-parallel=" + parallel
                    , "-template=" + template.getAbsolutePath()
                    , "-templateParameterAttribute="
                    , "-result=" + result.getAbsolutePath()
                    , "-resultPath=$tableName$." + extension
            });
            for (final String name : GenerateTableParallelTest.TABLES) {
                try (final Workbook workbook = WorkbookFactory.create(new File(result, name + "." + extension))) {
                    final Sheet sheet = workbook.getSheetAt(0);
                    Assertions.assertEquals(name, sheet.getRow(0).getCell(0).getStringCellValue(), parallel);
                    for (int i = 0; i < 100; i++) {
                        Assertions.assertEquals(name + i, sheet.getRow(i + 1).getCell(0).getStringCellValue(), parallel);
                        Assertions.assertEquals(String.valueOf(i), sheet.getRow(i + 1).getCell(1).getStringCellValue(), parallel);
                    }
                }
            }
        }
    }

    private File excelTemplate(final String extension) throws IOException {
        final File result = this.tempDir.resolve("template." + extension).toFile();
        try (final Workbook workbook = "xls".equals(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
             final FileOutputStream out = new FileOutputStream(result)) {
            final Sheet sheet = workbook.createSheet("table");
            sheet.createRow(0).createCell(0).setCellValue("${tableName}");
            final Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("${row.ID}");
            row.createCell(1).setCellValue("${row.VAL}");
            this.addComment(sheet, 0, "jx:area(lastCell=\"B2\")");
            this.addComment(sheet, 1, "jx:each(items=\"rows\" var=\"row\" lastCell=\"B2\")");
            workbook.write(out);
        }
        return result;
    }

    private void addComment(final Sheet sheet, final int rowIndex, final String text) {
        final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        final ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(0);
        anchor.setCol2(2);
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 2);
        final Comment comment = sheet.createDrawingPatriarch().createCellComment(anchor);
        comment.setString(factory.createRichTextString(text));
        sheet.getRow(rowIndex).getCell(0).setCellComment(comment);
    }

    private String[] txtArgs(final File result, final String resultPath) {
        return new String[]{
                "-srcType=csv"
                , "-src=" + this.src.getAbsolutePath()
                , "-encoding=UTF-8"
                , "-generateType=txt"
                , "-unit=table"
                , "-template=" + this.template.getAbsolutePath()
                , "-templateParameterAttribute="
                , "-template.encoding=UTF-8"
                , "-outputEncoding=UTF-8"
                , "-result=" + result.getAbsolutePath()
                , "-resultPath=" + resultPath
        };
    }

    private String expected(final String name) {
        return name + ":" + IntStream.range(0, 100).mapToObj(i -> name + i).collect(Collectors.joining(","));
    }

    private int count(final Statement stmt, final String from) throws SQLException {
        try (final ResultSet rs = stmt.executeQuery("select count(*) from " + from)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
