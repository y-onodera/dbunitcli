package yo.dbunitcli.dataset;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.json.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.application.json.TableMetaDataFilterParser;
import yo.dbunitcli.dataset.converter.CsvConverter;
import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.converter.XlsConverter;
import yo.dbunitcli.dataset.converter.XlsxConverter;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;

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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParallelTableMappingTest {

    @TempDir
    Path tempDir;

    private File src;

    @BeforeEach
    public void setUp() throws IOException {
        this.src = Files.createDirectories(this.tempDir.resolve("src")).toFile();
        for (final String name : List.of("a", "b", "c", "d")) {
            final String body = IntStream.range(0, 100)
                    .mapToObj(i -> "\"" + name + i + "\",\"" + i + "\"")
                    .collect(Collectors.joining("\n", "\"ID\",\"VAL\"\n", "\n"));
            Files.writeString(new File(this.src, name + ".csv").toPath(), body, StandardCharsets.UTF_8);
        }
    }

    @Test
    public void lanes_group_sources_writing_same_table() {
        final List<List<ComparableTableMappingTask>> lanes = ParallelTableMapping.lanes(
                this.producer(this.renameBToA(), null, null), this.tasks(this.renameBToA())).orElseThrow();
        final Set<Set<String>> actual = lanes.stream()
                .map(lane -> lane.stream().map(it -> it.source().getTableName()).collect(Collectors.toSet()))
                .collect(Collectors.toSet());
        Assertions.assertEquals(Set.of(Set.of("a", "b"), Set.of("c"), Set.of("d")), actual);
    }

    @Test
    public void lanes_keep_source_order_in_lane() {
        final List<String> sourceOrder = this.tasks(this.renameBToA()).stream()
                .map(it -> it.source().getTableName())
                .filter(it -> it.equals("a") || it.equals("b"))
                .toList();
        final List<String> laneOrder = ParallelTableMapping.lanes(this.producer(this.renameBToA(), null, null), this.tasks(this.renameBToA()))
                .orElseThrow().stream()
                .filter(lane -> lane.size() == 2)
                .findFirst().orElseThrow().stream()
                .map(it -> it.source().getTableName())
                .toList();
        Assertions.assertEquals(sourceOrder, laneOrder);
    }

    @Test
    public void lanes_empty_when_parallel_is_one() {
        Assertions.assertTrue(ParallelTableMapping.lanes(this.producer(TableSeparators.NONE, null, 1), this.tasks(TableSeparators.NONE)).isEmpty());
    }

    @Test
    public void lanes_empty_when_converter_write_one_book() {
        final XlsxConverter converter = new XlsxConverter(this.tempDir.resolve("xlsx").toFile(), "result", XlsConverter.TableExportType.SHEET, true, true);
        Assertions.assertTrue(ParallelTableMapping.lanes(this.producer(TableSeparators.NONE, converter, null), this.tasks(TableSeparators.NONE)).isEmpty());
    }

    @Test
    public void lanes_empty_when_all_sources_write_same_table() {
        final TableSeparators allToA = new TableSeparators.Builder()
                .add(TableSeparators.NONE)
                .setCommonRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder().setNewName("a").build())
                .build();
        Assertions.assertTrue(ParallelTableMapping.lanes(this.producer(allToA, null, null), this.tasks(allToA)).isEmpty());
    }

    @Test
    public void produce_without_converter_same_as_sequential() {
        final ComparableDataSet sequential = this.producer(this.renameBToA(), null, 1).loadDataSet();
        final ComparableDataSet parallel = this.producer(this.renameBToA(), null, 4).loadDataSet();
        Assertions.assertArrayEquals(new String[]{"a", "c", "d"}, parallel.getTableNames());
        Assertions.assertArrayEquals(sequential.getTableNames(), parallel.getTableNames());
        for (final String tableName : sequential.getTableNames()) {
            Assertions.assertEquals(this.rows(sequential.getTable(tableName)), this.rows(parallel.getTable(tableName)));
        }
        Assertions.assertEquals(200, parallel.getTable("a").getRowCount());
    }

    @Test
    public void produce_with_converter_same_as_sequential() throws IOException {
        final File sequentialDir = this.tempDir.resolve("sequential").toFile();
        final File parallelDir = this.tempDir.resolve("parallel").toFile();
        this.producer(this.renameBToA(), new CsvConverter(sequentialDir, "UTF-8"), 1).loadDataSet();
        this.producer(this.renameBToA(), new CsvConverter(parallelDir, "UTF-8"), 4).loadDataSet();
        final List<String> files = Arrays.stream(Objects.requireNonNull(sequentialDir.list())).sorted().toList();
        Assertions.assertEquals(List.of("a.csv", "c.csv", "d.csv"), files);
        Assertions.assertEquals(files, Arrays.stream(Objects.requireNonNull(parallelDir.list())).sorted().toList());
        for (final String file : files) {
            Assertions.assertEquals(Files.readString(new File(sequentialDir, file).toPath())
                    , Files.readString(new File(parallelDir, file).toPath()));
        }
    }

    @Test
    public void lanes_empty_when_db_converter_has_no_connection_loader() throws SQLException {
        final String url = "jdbc:h2:mem:parallelNoLoader;DB_CLOSE_DELAY=-1";
        final DBConverter converter = new DBConverter(this.connectionLoader(url).loadConnection(), DbOperation.INSERT, true);
        Assertions.assertTrue(ParallelTableMapping.lanes(this.producer(TableSeparators.NONE, converter, null), this.tasks(TableSeparators.NONE)).isEmpty());
    }

    @Test
    public void produce_to_table_with_connection_per_lane() throws SQLException {
        final String url = "jdbc:h2:mem:parallelTable;DB_CLOSE_DELAY=-1";
        try (final Connection conn = DriverManager.getConnection(url, "sa", "");
             final Statement stmt = conn.createStatement()) {
            for (final String table : List.of("a", "c", "d")) {
                stmt.execute("create table " + table + " (ID varchar(10) primary key, VAL varchar(10))");
                stmt.execute("insert into " + table + " values ('old', '0')");
            }
            final IDataSetConverter converter = new DataSetConverterLoader().get(DataSetConverterParam.builder()
                    .setResultType(ResultType.table)
                    .setOperation(DbOperation.CLEAN_INSERT)
                    .setDatabaseConnectionLoader(this.connectionLoader(url))
                    .setExportEmptyTable(true)
                    .build());
            Assertions.assertTrue(converter.isParallelizable());
            final ComparableCsvDataSetProducer producer = this.producer(this.renameBToA(), converter, 4);
            Assertions.assertEquals(3, ParallelTableMapping.lanes(producer, this.tasks(this.renameBToA())).orElseThrow().size());

            producer.loadDataSet();

            // CLEAN_INSERTはテーブルごとに1度だけtruncateし、同じテーブルへの2ソース目は追記する
            Assertions.assertEquals(200, this.count(stmt, "a"));
            Assertions.assertEquals(100, this.count(stmt, "c"));
            Assertions.assertEquals(100, this.count(stmt, "d"));
            Assertions.assertEquals(0, this.count(stmt, "a where ID = 'old'"));
            Assertions.assertEquals(100, this.count(stmt, "a where ID like 'b%'"));
        }
    }

    private DatabaseConnectionLoader connectionLoader(final String url) {
        final Properties prop = new Properties();
        prop.put("url", url);
        prop.put("user", "sa");
        prop.put("pass", "");
        return new DatabaseConnectionLoader(prop);
    }

    private int count(final Statement stmt, final String from) throws SQLException {
        try (final ResultSet rs = stmt.executeQuery("select count(*) from " + from)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    public void lanes_group_xlsx_by_sheet_name() throws IOException {
        final File xlsx = this.writeXlsx(Map.of("x1", List.of("s1", "s2"), "x2", List.of("s2", "s3"), "x3", List.of("s4"), "x4", List.of("s5")));
        final ComparableXlsxDataSetProducer producer = this.xlsxProducer(xlsx, XlsxSchema.DEFAULT, null);
        Assertions.assertEquals(Set.of(Set.of("x1.xlsx", "x2.xlsx"), Set.of("x3.xlsx"), Set.of("x4.xlsx")), this.laneFileNames(producer));
    }

    @Test
    public void lanes_group_xlsx_by_schema_table_name() throws IOException {
        // パターンに一致するシート（M_A・M_B）は別名でも同じテーブルMになる。スキーマに無いシートは読まない
        final XlsxSchema schema = new FromJsonXlsxSchemaBuilder().build("""
                {"patterns": {"M": "M_"}
                , "rows": [{"tableName": "M", "sheetName": "M", "header": ["ID", "VAL"], "dataStart": 1, "columnIndex": [0, 1], "breakKey": []}]}
                """);
        final File xlsx = this.writeXlsx(Map.of("x1", List.of("s1", "M_A"), "x2", List.of("M_B"), "x3", List.of("s1")));
        final ComparableXlsxDataSetProducer producer = this.xlsxProducer(xlsx, schema, null);
        Assertions.assertEquals(Set.of(Set.of("x1.xlsx", "x2.xlsx"), Set.of("x3.xlsx")), this.laneFileNames(producer));
    }

    @Test
    public void produce_xlsx_same_as_sequential() throws IOException {
        final File xlsx = this.writeXlsx(Map.of("x1", List.of("s1", "s2"), "x2", List.of("s2", "s3"), "x3", List.of("s4")));
        final ComparableDataSet sequential = this.xlsxProducer(xlsx, XlsxSchema.DEFAULT, 1).loadDataSet();
        final ComparableDataSet parallel = this.xlsxProducer(xlsx, XlsxSchema.DEFAULT, 4).loadDataSet();
        Assertions.assertArrayEquals(new String[]{"s1", "s2", "s3", "s4"}, parallel.getTableNames());
        for (final String tableName : sequential.getTableNames()) {
            Assertions.assertEquals(this.rows(sequential.getTable(tableName)), this.rows(parallel.getTable(tableName)));
        }
        Assertions.assertEquals(20, parallel.getTable("s2").getRowCount());
    }

    private Set<Set<String>> laneFileNames(final ComparableDataSetProducer producer) {
        final List<ComparableTableMappingTask> tasks = producer.getSourceStream().map(producer::createTableMappingTask).collect(Collectors.toList());
        return ParallelTableMapping.lanes(producer, tasks).orElseThrow().stream()
                .map(lane -> lane.stream().map(it -> it.source().fileName()).collect(Collectors.toSet()))
                .collect(Collectors.toSet());
    }

    private File writeXlsx(final Map<String, List<String>> sheetsByFile) throws IOException {
        final File dir = Files.createDirectories(this.tempDir.resolve("xlsx")).toFile();
        for (final Map.Entry<String, List<String>> file : sheetsByFile.entrySet()) {
            try (final XSSFWorkbook workbook = new XSSFWorkbook();
                 final FileOutputStream out = new FileOutputStream(new File(dir, file.getKey() + ".xlsx"))) {
                for (final String sheetName : file.getValue()) {
                    final Sheet sheet = workbook.createSheet(sheetName);
                    final Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("ID");
                    header.createCell(1).setCellValue("VAL");
                    for (int i = 1; i <= 10; i++) {
                        final Row row = sheet.createRow(i);
                        row.createCell(0).setCellValue(file.getKey() + "-" + i);
                        row.createCell(1).setCellValue(sheetName);
                    }
                }
                workbook.write(out);
            }
        }
        return dir;
    }

    private ComparableXlsxDataSetProducer xlsxProducer(final File src, final XlsxSchema schema, final Integer parallel) {
        return new ComparableXlsxDataSetProducer(ComparableDataSetParam.builder()
                .setSrc(src)
                .setSource(DataSourceType.xlsx)
                .setXlsxSchema(schema)
                .setParallel(parallel)
                .build());
    }

    private TableSeparators renameBToA() {
        final TableSeparators.Builder builder = new TableSeparators.Builder();
        builder.addSetting(TableSeparator.builder()
                .setTargetFilter(TableMetaDataFilterParser.any("b"))
                .setSplitter(new TableSplitter.Builder()
                        .setRenameFunction(new TableRenameStrategy.ReplaceFunction.Builder().setNewName("a").build())
                        .build())
                .build());
        return builder.build();
    }

    private ComparableCsvDataSetProducer producer(final TableSeparators tableSeparators, final IDataSetConverter converter, final Integer parallel) {
        return new ComparableCsvDataSetProducer(ComparableDataSetParam.builder()
                .setSrc(this.src)
                .setSource(DataSourceType.csv)
                .setEncoding("UTF-8")
                .setTableSeparators(tableSeparators)
                .setConverter(converter)
                .setParallel(parallel)
                .build());
    }

    private List<ComparableTableMappingTask> tasks(final TableSeparators tableSeparators) {
        final ComparableCsvDataSetProducer producer = this.producer(tableSeparators, null, null);
        return producer.getSourceStream().map(producer::createTableMappingTask).collect(Collectors.toList());
    }

    private List<List<Object>> rows(final ComparableTable table) {
        return IntStream.range(0, table.getRowCount())
                .mapToObj(i -> Arrays.asList(table.getRow(i)))
                .toList();
    }
}
