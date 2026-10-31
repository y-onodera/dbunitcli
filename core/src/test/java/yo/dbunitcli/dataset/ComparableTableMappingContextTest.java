package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
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

    @Test
    public void mapで編集した設定でもjoinの結果の行は増えない() {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(DataSetTestSupport.separators("""
                {"settings": [{"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """).map(it -> {
        }), null);
        context.open();
        DataSetTestSupport.write(context, "a", "1", "2", "3");
        DataSetTestSupport.write(context, "b", "1", "2", "3");
        Assertions.assertEquals(3, context.close().get("a_with_b").getRowCount());
    }

    @Test
    public void 並べ替えて分割しても行にソースでの行番号が対応する() {
        Assertions.assertEquals(Map.of("1", 2, "2", 4, "3", 1, "4", 3, "5", 0), ComparableTableMappingContextTest.originalRowNumbers("""
                {"settings": [{"name": "a", "order": ["key"], "split": {"suffix": "_%02d", "limit": 2}}]}
                """));
    }

    @Test
    public void filterで除いた行もソースでの行番号に数える() {
        Assertions.assertEquals(Map.of("1", 2, "2", 4, "4", 3, "5", 0), ComparableTableMappingContextTest.originalRowNumbers("""
                {"settings": [{"name": "a", "order": ["key"], "filter": ["key != '3'"]}]}
                """));
        Assertions.assertEquals(Map.of("1", 2, "2", 4, "4", 3, "5", 0), ComparableTableMappingContextTest.originalRowNumbers("""
                {"settings": [{"name": "a", "order": ["key"], "filter": ["key != '3'"], "split": {"suffix": "_%02d", "limit": 2}}]}
                """));
    }

    @Test
    public void distinctで除いた行もソースでの行番号に数える() {
        Assertions.assertEquals(Map.of("1", 2, "2", 4, "3", 1, "4", 3, "5", 0), ComparableTableMappingContextTest.originalRowNumbers("""
                {"settings": [{"name": "a", "distinct": true}]}
                """, "5", "3", "1", "4", "2", "3", "1"));
    }

    /**
     * テーブルaに5,3,1,4,2の順で行を書き、出力した行のkeyごとのソースでの行番号（compareの$ROW_ORIGINAL）を返す
     */
    private static Map<String, Integer> originalRowNumbers(final String setting) {
        return ComparableTableMappingContextTest.originalRowNumbers(setting, "5", "3", "1", "4", "2");
    }

    private static Map<String, Integer> originalRowNumbers(final String setting, final String... keys) {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(DataSetTestSupport.separators(setting), null);
        context.open();
        DataSetTestSupport.write(context, "a", keys);
        final Map<String, Integer> result = new LinkedHashMap<>();
        context.close().values().forEach(table -> table.getRows(List.of("key"))
                                                       .values()
                                                       .forEach(row -> result.put(row.getValue()[0].toString(), row.getKey())));
        return result;
    }

    /**
     * key列に1〜3の3行を持つテーブルtableNamesを読み込み、出力したテーブルごとの行数を返す
     */
    private static Map<String, Integer> load(final String setting, final String... tableNames) {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(DataSetTestSupport.separators(setting), null);
        context.open();
        Stream.of(tableNames).forEach(tableName -> DataSetTestSupport.write(context, tableName, "1", "2", "3"));
        final Map<String, Integer> result = new LinkedHashMap<>();
        context.close().forEach((tableName, table) -> result.put(tableName, table.getRowCount()));
        return result;
    }
}
