package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import yo.dbunitcli.application.json.TableMetaDataFilterParser;
import yo.dbunitcli.dataset.converter.CsvConverter;
import yo.dbunitcli.dataset.converter.XlsConverter;
import yo.dbunitcli.dataset.converter.XlsxConverter;
import yo.dbunitcli.dataset.producer.ComparableCsvDataSetProducer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    .collect(Collectors.joining("\n", "\"KEY\",\"VALUE\"\n", "\n"));
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
