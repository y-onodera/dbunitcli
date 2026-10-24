package yo.dbunitcli.application.command;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import yo.dbunitcli.TestJdbc;
import yo.dbunitcli.application.ParameterUnit;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMappingTask;
import yo.dbunitcli.dataset.DataSourceType;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
            final int before = TestJdbc.sessions(stmt);
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
            // srcを読む元の接続も、2つ目以降の作業スレッドで張った接続も、処理終了時に閉じる
            Assertions.assertEquals(before, TestJdbc.sessions(stmt));
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

    @ParameterizedTest
    @CsvSource({"ddl,.sql", "javaBean,.java"})
    public void generate_wrapped_type_from_db_same_as_sequential(final String generateType, final String extension) throws SQLException, IOException {
        final String url = "jdbc:h2:mem:generateWrappedParallel" + generateType + ";DB_CLOSE_DELAY=-1";
        final File listFile = this.tempDir.resolve("tables.txt").toFile();
        final List<String> tables = List.of("T1", "T2", "T3", "T4");
        try (final Connection conn = DriverManager.getConnection(url, "sa", "sa");
             final Statement stmt = conn.createStatement()) {
            for (final String table : tables) {
                stmt.execute("create table " + table + " (ID int not null, NAME varchar(" + (10 + tables.indexOf(table))
                        + ") not null, MEMO varchar(100), constraint PK_" + table + " primary key (ID))");
                stmt.execute("comment on table " + table + " is '" + table + " remarks'");
                stmt.execute("comment on column " + table + ".NAME is '" + table + " name'");
            }
            Files.writeString(listFile.toPath(), String.join("\n", tables), StandardCharsets.UTF_8);
            final int before = TestJdbc.sessions(stmt);
            final Map<String, String> results = new HashMap<>();
            for (final String parallel : List.of("", "1")) {
                final File result = this.tempDir.resolve("result-" + generateType + parallel).toFile();
                Generate.main(new String[]{
                        "-generateType=" + generateType
                        , "-srcType=table"
                        , "-src=" + listFile.getAbsolutePath()
                        , "-jdbcUrl=" + url
                        , "-jdbcUser=sa"
                        , "-jdbcPass=sa"
                        , "-parallel=" + parallel
                        , "-result=" + result.getAbsolutePath()
                        , "-resultPath=out"
                });
                try (final Stream<Path> files = Files.list(result.toPath().resolve("out"))) {
                    for (final Path file : files.toList()) {
                        final String content = Files.readString(file, StandardCharsets.UTF_8);
                        final String previous = results.putIfAbsent(file.getFileName().toString(), content);
                        if (previous != null) {
                            Assertions.assertEquals(previous, content, file.getFileName().toString());
                        }
                    }
                }
            }
            Assertions.assertEquals(tables.size(), results.size(), results.keySet().toString());
            // テーブルごとのコメント・桁数が混ざらない（作業スレッドごとの接続からメタデータを取る）
            for (final String table : tables) {
                final String content = results.entrySet().stream()
                        .filter(it -> it.getKey().toUpperCase().startsWith(table) && it.getKey().endsWith(extension))
                        .map(Map.Entry::getValue)
                        .findFirst().orElseThrow(() -> new AssertionError(table + " " + results.keySet()));
                if ("ddl".equals(generateType)) {
                    Assertions.assertTrue(content.contains(table + " remarks"), content);
                    Assertions.assertTrue(content.contains("VARYING(" + (10 + tables.indexOf(table)) + ")"), content);
                    Assertions.assertTrue(content.contains("PK_" + table), content);
                }
            }
            // 作業スレッドで張った接続も閉じる
            Assertions.assertEquals(before, TestJdbc.sessions(stmt));
        }
    }

    @ParameterizedTest
    @CsvSource({"txt,true", "txt,false", "xlsx,false"})
    public void generate_record_same_as_sequential(final String generateType, final boolean includeMetaData) throws IOException {
        final File template = "txt".equals(generateType)
                ? this.template
                : TestJxlsTemplates.create(this.tempDir, generateType);
        final Map<String, byte[]> results = new HashMap<>();
        for (final String parallel : List.of("", "1")) {
            final File result = this.tempDir.resolve("result-record-" + generateType + includeMetaData + parallel).toFile();
            Generate.main(new String[]{
                    "-srcType=csv"
                    , "-src=" + this.src.getAbsolutePath()
                    , "-encoding=UTF-8"
                    , "-src.includeMetaData=" + includeMetaData
                    // Excelは1行1ブックで重いため1テーブルに絞る
                    , "-regInclude=" + ("txt".equals(generateType) ? "" : "a")
                    , "-generateType=" + generateType
                    , "-unit=record"
                    , "-parallel=" + parallel
                    , "-template=" + template.getAbsolutePath()
                    , "-templateParameterAttribute="
                    , "-result=" + result.getAbsolutePath()
                    , "-resultPath=$tableName$_$rowNumber$." + generateType
            });
            try (final Stream<Path> files = Files.list(result.toPath())) {
                for (final Path file : files.toList()) {
                    final byte[] content = "txt".equals(generateType) ? Files.readAllBytes(file) : this.cellValues(file);
                    final byte[] previous = results.putIfAbsent(file.getFileName().toString(), content);
                    if (previous != null) {
                        Assertions.assertArrayEquals(previous, content, file.getFileName().toString());
                    }
                }
            }
        }
        // rowNumberはテーブルごとに0から振る
        final List<String> tables = "txt".equals(generateType) ? GenerateTableParallelTest.TABLES : List.of("a");
        Assertions.assertEquals(tables.stream()
                                      .flatMap(table -> IntStream.range(0, 100).mapToObj(i -> table + "_" + i + "." + generateType))
                                      .collect(Collectors.toSet()), results.keySet());
    }

    @Test
    public void generate_record_to_same_file_keeps_last_row() throws IOException {
        // 同じレーン（同じテーブル）の行が同じファイルへ出力する場合は、同じスレッドで元の順に書くため最後の行の内容が残る
        // （別のレーンから同じファイルへ書く場合、残る内容は不定）
        final File template = this.tempDir.resolve("record.txt").toFile();
        Files.writeString(template.toPath(), "$ID$", StandardCharsets.UTF_8);
        final File result = this.tempDir.resolve("result").toFile();
        Generate.main(new String[]{
                "-srcType=csv"
                , "-src=" + this.src.getAbsolutePath()
                , "-encoding=UTF-8"
                , "-regInclude=a"
                , "-generateType=txt"
                , "-unit=record"
                , "-template=" + template.getAbsolutePath()
                , "-templateParameterAttribute="
                , "-result=" + result.getAbsolutePath()
                , "-resultPath=all.txt"
        });
        Assertions.assertEquals("a99", Files.readString(new File(result, "all.txt").toPath(), StandardCharsets.UTF_8));
    }

    private byte[] cellValues(final Path file) throws IOException {
        try (final Workbook workbook = WorkbookFactory.create(file.toFile())) {
            final StringBuilder result = new StringBuilder();
            workbook.getSheetAt(0).forEach(row -> row.forEach(cell -> result.append(cell.toString()).append(',')));
            return result.toString().getBytes(StandardCharsets.UTF_8);
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
        final File template = TestJxlsTemplates.create(this.tempDir, extension);
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
}
