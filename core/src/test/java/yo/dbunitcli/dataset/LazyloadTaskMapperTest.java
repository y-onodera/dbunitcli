package yo.dbunitcli.dataset;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.common.Source;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private List<String> tableNames(final String setting) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                                                                   .setTableSeparators(LazyloadTaskMapperTest.separators(setting))
                                                                   .build();
        return LazyloadTaskMapper.mappingFrom(param, Stream.of(new RecordingTask("a", param, this.rowsRead)
                                                     , new RecordingTask("b", param, this.rowsRead)))
                                 .stream()
                                 .map(ComparableTableDto::getTableName)
                                 .toList();
    }

    private static TableSeparators separators(final String setting) {
        try (final JsonReader reader = Json.createReader(new StringReader(setting))) {
            return new FromJsonTableSeparatorsBuilder("UTF-8").configureSetting(reader.readObject()).build();
        }
    }

    /**
     * key列に1〜3の3行を持つテーブル。行を読んだか（loadDataとhasOutput）を記録する
     */
    private record RecordingTask(String tableName, ComparableDataSetParam param, Map<String, Boolean> rowsRead)
            implements ComparableTableMappingTask {

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
                Stream.of("1", "2", "3").forEach(key -> mapper.addRow(new Object[]{key}));
            }
            mapper.endTable();
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new RecordingTask(this.tableName, builder.build(), this.rowsRead);
        }
    }
}
