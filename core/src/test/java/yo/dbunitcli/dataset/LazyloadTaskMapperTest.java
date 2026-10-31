package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LazyloadTaskMapperTest {

    private final Map<String, Boolean> rowsRead = new LinkedHashMap<>();

    @Test
    public void 分割するテーブルの行だけを読む() {
        final List<String> actual = this.tableNames("""
                {"settings": [{"name": "a", "split": {"suffix": "_%02d", "limit": 2}}]}
                """);

        Assertions.assertEquals(List.of("a_00", "a_01", "b"), actual);
        Assertions.assertEquals(Map.of("a", true, "b", false), this.rowsRead);
    }

    @Test
    public void joinの結果を分割するならjoinに関係するテーブルの行を読む() {
        final List<String> actual = this.tableNames("""
                {"settings": [{"innerJoin": {"left": "a", "right": "b", "column": ["key"]}
                             , "split": {"tableName": "joined", "suffix": "_%02d", "limit": 1}}]}
                """);

        Assertions.assertEquals(List.of("joined_00", "joined_01", "joined_02"), actual);
        Assertions.assertEquals(Map.of("a", true, "b", true), this.rowsRead);
    }

    @Test
    public void 分割が無ければ行を読まない() {
        final List<String> actual = this.tableNames("""
                {"settings": [{"name": "a", "tableName": "renamed"}]}
                """);

        Assertions.assertEquals(List.of("b", "renamed"), actual);
        Assertions.assertEquals(Map.of("a", false, "b", false), this.rowsRead);
    }

    @Test
    public void 同じテーブルに複数のソースから行が入る分割は_どの分割後のテーブルも全ソースから読む() {
        Assertions.assertEquals(Map.of("a_00", 2, "a_01", 2, "a_02", 2), this.loadedRowCounts("""
                {"settings": [{"name": "a", "split": {"suffix": "_%02d", "limit": 2}}]}
                """, 2));
    }

    @Test
    public void 同じテーブルに複数のソースから行が入る分割は_行を保持して並べ替える場合もどの分割後のテーブルも全ソースから読む() {
        Assertions.assertEquals(Map.of("a_00", 2, "a_01", 2, "a_02", 2), this.loadedRowCounts("""
                {"settings": [{"name": "a", "order": ["key"], "split": {"suffix": "_%02d", "limit": 2}}]}
                """, 2));
    }

    @Test
    public void 同じテーブルに3つ以上のソースから行が入ると_全ソースから読む() {
        Assertions.assertEquals(Map.of("a", 9), this.loadedRowCounts("""
                {"settings": [{"name": "a", "order": ["key"]}]}
                """, 3));
    }

    @Test
    public void 後に並ぶjoinの結果を使うjoinは_使うjoinの左右のテーブルも読む() {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                                                                   .setTableSeparators(DataSetTestSupport.separators("""
                                                                           {"settings": [{"innerJoin": {"left": "a_with_b", "right": "c", "column": ["key"]}}
                                                                                       , {"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                                                                           """))
                                                                   .build();
        final Map<String, Set<String>> actual = new LinkedHashMap<>();
        LazyloadTaskMapper.mappingFrom(param, Stream.of("a", "b", "c").map(name -> new RecordingTask(name, param, this.rowsRead)))
                          .forEach(dto -> actual.put(dto.getTableName(), dto.getTask().tasks().stream()
                                                                            .map(task -> task.source().getTableName())
                                                                            .collect(Collectors.toSet())));

        Assertions.assertEquals(Set.of("a", "b", "c"), actual.get("a_with_b_with_c"));
    }

    @Test
    public void joinの結果を読み直すとjoinの元テーブルも読む() {
        Assertions.assertEquals(Map.of("a_with_b", 3), this.loadedRowCounts("""
                {"settings": [{"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """, "a", "b"));
    }

    /**
     * tableNamesのテーブル（3行ずつ）を読み、出力されたテーブルごとに読み直した行数を返す
     */
    private Map<String, Integer> loadedRowCounts(final String setting, final String... tableNames) {
        return this.loadedRowCounts(setting, param -> Stream.of(tableNames).map(name -> new RecordingTask(name, param, this.rowsRead)));
    }

    /**
     * sources個のソースがaに3行ずつ書き、出力されたテーブルごとに読み直した行数を返す
     */
    private Map<String, Integer> loadedRowCounts(final String setting, final int sources) {
        return this.loadedRowCounts(setting, param -> IntStream.range(0, sources)
                                                               .mapToObj(no -> new RecordingTask("a", param, this.rowsRead
                                                                       , no + "1", no + "2", no + "3")));
    }

    private Map<String, Integer> loadedRowCounts(final String setting
            , final Function<ComparableDataSetParam, Stream<RecordingTask>> tasks) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                                                                   .setTableSeparators(DataSetTestSupport.separators(setting))
                                                                   .build();
        final Map<String, Integer> result = new LinkedHashMap<>();
        LazyloadTaskMapper.mappingFrom(param, tasks.apply(param).map(ComparableTableMappingTask.class::cast))
                          .forEach(dto -> result.put(dto.getTableName()
                                  , ((List<?>) dto.applyDataSetSeparators(TableSeparators.NONE).getRows()).size()));
        return result;
    }

    private List<String> tableNames(final String setting) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                                                                   .setTableSeparators(DataSetTestSupport.separators(setting))
                                                                   .build();
        return LazyloadTaskMapper.mappingFrom(param, Stream.of(new RecordingTask("a", param, this.rowsRead)
                                                     , new RecordingTask("b", param, this.rowsRead)))
                                 .stream()
                                 .map(ComparableTableDto::getTableName)
                                 .toList();
    }

    /**
     * key列にkeys（既定は1〜3）の行を持つテーブル。行を読んだか（loadDataとhasOutput）を記録する
     */
    private record RecordingTask(String tableName, ComparableDataSetParam param, Map<String, Boolean> rowsRead
            , String... keys) implements ComparableTableMappingTask {

        private RecordingTask(final String tableName, final ComparableDataSetParam param, final Map<String, Boolean> rowsRead) {
            this(tableName, param, rowsRead, "1", "2", "3");
        }

        @Override
        public Source source() {
            return Source.NONE.tableName(this.tableName);
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            final ComparableTableMapper mapper = context.createMapper(this.source().createMetaData(new String[]{"key"}));
            mapper.startTable();
            final boolean read = this.param.loadData() && mapper.hasOutput();
            this.rowsRead.put(this.tableName, read);
            if (read) {
                Stream.of(this.keys).forEach(key -> mapper.addRow(new Object[]{key}));
            }
            mapper.endTable();
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new RecordingTask(this.tableName, builder.build(), this.rowsRead, this.keys);
        }
    }
}
