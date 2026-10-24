package yo.dbunitcli.dataset;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.json.FromJsonXlsxSchemaBuilder;
import yo.dbunitcli.application.json.TableMetaDataFilterParser;
import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.converter.CsvConverter;
import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.dataset.converter.DataSetConverterLoader;
import yo.dbunitcli.dataset.converter.XlsConverter;
import yo.dbunitcli.dataset.converter.XlsxConverter;
import yo.dbunitcli.dataset.producer.ComparableCSVQueryDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDBDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableDataSetLoader;
import yo.dbunitcli.dataset.producer.ComparableQueryDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsDataSetProducer;
import yo.dbunitcli.dataset.producer.ComparableXlsxDataSetProducer;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.resource.TestThreadContextPropagator;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
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
        this.assertSameTables(sequential, parallel);
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
        final File xlsx = this.writeExcel(Map.of("x1", List.of("s1", "s2"), "x2", List.of("s2", "s3"), "x3", List.of("s4"), "x4", List.of("s5")), "xlsx");
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
        final File xlsx = this.writeExcel(Map.of("x1", List.of("s1", "M_A"), "x2", List.of("M_B"), "x3", List.of("s1")), "xlsx");
        final ComparableXlsxDataSetProducer producer = this.xlsxProducer(xlsx, schema, null);
        Assertions.assertEquals(Set.of(Set.of("x1.xlsx", "x2.xlsx"), Set.of("x3.xlsx")), this.laneFileNames(producer));
    }

    @Test
    public void produce_xlsx_same_as_sequential() throws IOException {
        final File xlsx = this.writeExcel(Map.of("x1", List.of("s1", "s2"), "x2", List.of("s2", "s3"), "x3", List.of("s4")), "xlsx");
        final ComparableDataSet sequential = this.xlsxProducer(xlsx, XlsxSchema.DEFAULT, 1).loadDataSet();
        final ComparableDataSet parallel = this.xlsxProducer(xlsx, XlsxSchema.DEFAULT, 4).loadDataSet();
        Assertions.assertArrayEquals(new String[]{"s1", "s2", "s3", "s4"}, parallel.getTableNames());
        this.assertSameTables(sequential, parallel);
        Assertions.assertEquals(20, parallel.getTable("s2").getRowCount());
    }

    @Test
    public void lanes_group_xls_by_sheet_name() throws IOException {
        final File xls = this.writeExcel(Map.of("x1", List.of("s1", "s2"), "x2", List.of("s2", "s3"), "x3", List.of("s4"), "x4", List.of("s5")), "xls");
        final ComparableXlsDataSetProducer producer = this.xlsProducer(xls, null);
        Assertions.assertEquals(Set.of(Set.of("x1.xls", "x2.xls"), Set.of("x3.xls"), Set.of("x4.xls")), this.laneFileNames(producer));
    }

    @Test
    public void produce_xls_same_as_sequential() throws IOException {
        final File xls = this.writeExcel(Map.of("x1", List.of("s1", "s2"), "x2", List.of("s2", "s3"), "x3", List.of("s4")), "xls");
        final ComparableDataSet sequential = this.xlsProducer(xls, 1).loadDataSet();
        final ComparableDataSet parallel = this.xlsProducer(xls, 4).loadDataSet();
        Assertions.assertArrayEquals(new String[]{"s1", "s2", "s3", "s4"}, parallel.getTableNames());
        this.assertSameTables(sequential, parallel);
        Assertions.assertEquals(20, parallel.getTable("s2").getRowCount());
    }

    @Test
    public void produce_csvq_same_as_sequential() throws IOException {
        final File queryDir = Files.createDirectories(this.tempDir.resolve("csvq")).toFile();
        for (final String name : List.of("a", "b", "c", "d")) {
            final String csv = new File(this.src, name + ".csv").getAbsolutePath().replace("\\", "/");
            Files.writeString(new File(queryDir, "q" + name + ".sql").toPath()
                    , "SELECT * FROM CSVREAD('" + csv + "', null, 'charset=UTF-8') ORDER BY ID", StandardCharsets.UTF_8);
        }
        final ComparableDataSet sequential = this.csvqProducer(queryDir, 1).loadDataSet();
        final ComparableDataSet parallel = this.csvqProducer(queryDir, 4).loadDataSet();
        Assertions.assertArrayEquals(new String[]{"qa", "qb", "qc", "qd"}, parallel.getTableNames());
        this.assertSameTables(sequential, parallel);
        Arrays.stream(parallel.getTableNames()).forEach(it -> Assertions.assertEquals(100, parallel.getTable(it).getRowCount()));
    }

    @Test
    public void run_applies_registered_thread_context_propagator() {
        final Map<String, String> actual = new ConcurrentHashMap<>();
        final List<List<ComparableTableMappingTask>> lanes = List.of("a", "b").stream()
                .map(name -> List.of(this.captureTask(name, it -> actual.put(name, TestThreadContextPropagator.VALUE.orElse("unbound")))))
                .toList();
        ScopedValue.where(TestThreadContextPropagator.VALUE, "submitter")
                .run(() -> ParallelTableMapping.run(this.producer(TableSeparators.NONE, null, 2).param(), lanes));
        Assertions.assertEquals(Map.of("a", "submitter", "b", "submitter"), actual);
    }

    @Test
    public void run_propagates_file_resources_context_to_worker() {
        final String before = System.getProperty(FileResources.PROPERTY_DATASET_BASE);
        final File expected = this.tempDir.resolve("dataset").toFile();
        final Map<String, File> actual = new ConcurrentHashMap<>();
        final List<List<ComparableTableMappingTask>> lanes = List.of("a", "b").stream()
                .map(name -> List.of(this.captureTask(name, it -> actual.put(name, FileResources.datasetDir()))))
                .toList();
        try {
            System.setProperty(FileResources.PROPERTY_DATASET_BASE, expected.getPath());
            FileResources.runInContext(() -> {
                // 実行中にシステムプロパティが変わっても、作業スレッドは実行開始時のContextを参照する
                System.setProperty(FileResources.PROPERTY_DATASET_BASE, this.tempDir.resolve("other").toString());
                ParallelTableMapping.run(this.producer(TableSeparators.NONE, null, 2).param(), lanes);
            });
        } finally {
            if (before == null) {
                System.clearProperty(FileResources.PROPERTY_DATASET_BASE);
            } else {
                System.setProperty(FileResources.PROPERTY_DATASET_BASE, before);
            }
        }
        Assertions.assertEquals(Map.of("a", expected, "b", expected), actual);
    }

    private ComparableTableMappingTask captureTask(final String name, final Consumer<ComparableTableMappingContext> body) {
        final Source source = new Source(new File(this.src, name + ".csv"), false);
        final ComparableDataSetParam param = this.producer(TableSeparators.NONE, null, 2).param();
        return new ComparableTableMappingTask() {
            @Override
            public Source source() {
                return source;
            }

            @Override
            public ComparableDataSetParam param() {
                return param;
            }

            @Override
            public void run(final ComparableTableMappingContext context) {
                body.accept(context);
            }

            @Override
            public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
                return this;
            }
        };
    }

    @Test
    public void produce_from_table_and_sql_with_connection_per_lane() throws SQLException, IOException {
        final String url = "jdbc:h2:mem:parallelSource;DB_CLOSE_DELAY=-1";
        final File listFile = this.tempDir.resolve("tables.txt").toFile();
        final File queryDir = Files.createDirectories(this.tempDir.resolve("sql")).toFile();
        try (final Connection conn = DriverManager.getConnection(url, "sa", "");
             final Statement stmt = conn.createStatement()) {
            for (final String table : List.of("t1", "t2", "t3", "t4")) {
                stmt.execute("create table " + table + " (ID varchar(10) primary key, VAL varchar(10))");
                stmt.execute("insert into " + table + " select '" + table + "-' || x, x from system_range(1, 100)");
                Files.writeString(new File(queryDir, "q" + table + ".sql").toPath(), "select * from " + table + " order by ID", StandardCharsets.UTF_8);
            }
        }
        Files.writeString(listFile.toPath(), String.join("\n", "t1", "t2", "t3", "t4"), StandardCharsets.UTF_8);
        try (final Connection admin = DriverManager.getConnection(url, "sa", "");
             final Statement sessions = admin.createStatement()) {
            for (final Map.Entry<DataSourceType, File> src : Map.of(DataSourceType.table, listFile, DataSourceType.sql, queryDir).entrySet()) {
                final ComparableDataSetProducer producer = this.dbProducer(src.getKey(), src.getValue(), url, 4);
                Assertions.assertEquals(4, ParallelTableMapping.lanes(producer, this.tasks(producer)).orElseThrow().size(), src.getKey().name());
                final int before = this.count(sessions, "information_schema.sessions");
                final ComparableDataSet parallel = producer.loadDataSet();
                // 2つ目以降のレーンで張った接続はレーン終了時に閉じる
                Assertions.assertEquals(before, this.count(sessions, "information_schema.sessions"), src.getKey().name());
                final ComparableDataSet sequential = this.dbProducer(src.getKey(), src.getValue(), url, 1).loadDataSet();
                Assertions.assertArrayEquals(sequential.getTableNames(), parallel.getTableNames(), src.getKey().name());
                Assertions.assertEquals(4, parallel.getTableNames().length, src.getKey().name());
                this.assertSameTables(sequential, parallel);
                Arrays.stream(parallel.getTableNames()).forEach(it -> Assertions.assertEquals(100, parallel.getTable(it).getRowCount(), it));
            }
        }
    }

    private ComparableDataSetProducer dbProducer(final DataSourceType type, final File src, final String url, final Integer parallel) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                .setSrc(src)
                .setSource(type)
                .setEncoding("UTF-8")
                .setDatabaseConnectionLoader(this.connectionLoader(url))
                .setParallel(parallel)
                .build();
        return type == DataSourceType.sql
                ? new ComparableQueryDataSetProducer(param, Parameter.none())
                : new ComparableDBDataSetProducer(param);
    }

    private List<ComparableTableMappingTask> tasks(final ComparableDataSetProducer producer) {
        return producer.getSourceStream().map(producer::createTableMappingTask).collect(Collectors.toList());
    }

    private ComparableCSVQueryDataSetProducer csvqProducer(final File src, final Integer parallel) {
        return new ComparableCSVQueryDataSetProducer(ComparableDataSetParam.builder()
                .setSrc(src)
                .setSource(DataSourceType.csvq)
                .setEncoding("UTF-8")
                .setParallel(parallel)
                .build(), Parameter.none());
    }

    private Set<Set<String>> laneFileNames(final ComparableDataSetProducer producer) {
        return ParallelTableMapping.lanes(producer, this.tasks(producer)).orElseThrow().stream()
                .map(lane -> lane.stream().map(it -> it.source().fileName()).collect(Collectors.toSet()))
                .collect(Collectors.toSet());
    }

    private File writeExcel(final Map<String, List<String>> sheetsByFile, final String extension) throws IOException {
        final File dir = Files.createDirectories(this.tempDir.resolve(extension)).toFile();
        for (final Map.Entry<String, List<String>> file : sheetsByFile.entrySet()) {
            try (final Workbook workbook = "xls".equals(extension) ? new HSSFWorkbook() : new XSSFWorkbook();
                 final FileOutputStream out = new FileOutputStream(new File(dir, file.getKey() + "." + extension))) {
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

    private ComparableXlsDataSetProducer xlsProducer(final File src, final Integer parallel) {
        return new ComparableXlsDataSetProducer(ComparableDataSetParam.builder()
                .setSrc(src)
                .setSource(DataSourceType.xls)
                .setParallel(parallel)
                .build());
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
        return this.tasks(producer);
    }

    @Test
    public void data_source_type_matches_producer() {
        // 画面の表示判定（DataSourceType）と実行時の判定（producer）がずれないこと
        for (final DataSourceType type : DataSourceType.values()) {
            final ComparableDataSetParam param = ComparableDataSetParam.builder()
                    .setSrc(new File(this.src, "a.csv"))
                    .setSource(type)
                    .setFixedLength("1")
                    .setDatabaseConnectionLoader(this.connectionLoader("jdbc:h2:mem:sourceType;DB_CLOSE_DELAY=-1"))
                    .build();
            Assertions.assertEquals(type.isParallelizable()
                    , new ComparableDataSetLoader(Parameter.none()).getComparableDataSetProducer(param).isParallelizableSource()
                    , type.name());
        }
    }

    private void assertSameTables(final ComparableDataSet expected, final ComparableDataSet actual) {
        Assertions.assertArrayEquals(expected.getTableNames(), actual.getTableNames());
        for (final String tableName : expected.getTableNames()) {
            Assertions.assertEquals(this.rows(expected.getTable(tableName)), this.rows(actual.getTable(tableName)), tableName);
        }
    }

    private List<List<Object>> rows(final ComparableTable table) {
        return IntStream.range(0, table.getRowCount())
                .mapToObj(i -> Arrays.asList(table.getRow(i)))
                .toList();
    }
}
