package yo.dbunitcli.dataset;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.json.FromJsonTableSeparatorsBuilder;
import yo.dbunitcli.common.Source;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ComparableTableMappingContextTest {

    @Test
    public void outputFilterでjoinの結果を選んでもjoinの元テーブルは読む() {
        Assertions.assertEquals(Map.of("a_with_b", 3), ComparableTableMappingContextTest.load("""
                {"outputFilter": {"name": "a_with_b"}
                , "settings": [{"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """, "a", "b"));
    }

    @Test
    public void joinで使われずに残った元テーブルはoutputFilterで絞り込む() {
        Assertions.assertEquals(Map.of(), ComparableTableMappingContextTest.load("""
                {"outputFilter": {"name": "a_with_c"}
                , "settings": [{"innerJoin": {"left": "a", "right": "c", "column": ["key"]}}]}
                """, "a", "b"));
        Assertions.assertEquals(Map.of("a", 3, "b", 3), ComparableTableMappingContextTest.load("""
                {"settings": [{"innerJoin": {"left": "a", "right": "c", "column": ["key"]}}]}
                """, "a", "b"));
    }

    @Test
    public void joinの結果を使うjoinでは使われた元テーブルとjoinの結果を出力しない() {
        Assertions.assertEquals(Map.of("a_with_b_with_c", 3), ComparableTableMappingContextTest.load("""
                {"settings": [{"innerJoin": {"left": "a_with_b", "right": "c", "on": "a_with_b_a_key == c_key"}}
                            , {"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """, "a", "b", "c"));
    }

    /**
     * key列に1〜3の3行を持つテーブルtableNamesを読み込み、出力したテーブルごとの行数を返す
     */
    private static Map<String, Integer> load(final String setting, final String... tableNames) {
        final TableSeparators separators;
        try (final JsonReader reader = Json.createReader(new StringReader(setting))) {
            final JsonObject json = reader.readObject();
            separators = new FromJsonTableSeparatorsBuilder("UTF-8").configureSetting(json).configureOutputFilter(json).build();
        }
        final ComparableTableMappingContext context = new ComparableTableMappingContext(separators, null);
        context.open();
        Stream.of(tableNames).forEach(tableName -> {
            final ComparableTableMapper mapper = context.createMapper(Source.NONE.tableName(tableName).createMetaData(new String[]{"key"}));
            mapper.startTable();
            Stream.of("1", "2", "3").forEach(key -> mapper.addRow(new Object[]{key}));
            mapper.endTable();
        });
        final Map<String, Integer> result = new LinkedHashMap<>();
        context.close().forEach((tableName, table) -> result.put(tableName, table.getRowCount()));
        return result;
    }
}
