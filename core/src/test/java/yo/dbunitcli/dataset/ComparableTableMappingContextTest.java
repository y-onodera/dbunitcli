package yo.dbunitcli.dataset;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.application.SplitOrNameFilter;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.TableMetaDataFilter;

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
    public void joinで使われずに残った分割後の元テーブルは_joinの無い分割と同じく分割前の名前で絞り込む() {
        final String split = """
                {"settings": [{"name": "a", "split": {"suffix": "_%02d", "limit": 2}}
                            JOIN]}
                """;
        final TableMetaDataFilter filter = new SplitOrNameFilter("a_01");
        Assertions.assertEquals(Map.of("a_00", 2, "a_01", 1), ComparableTableMappingContextTest.load(
                DataSetTestSupport.separators(split.replace("JOIN", "")).filter(filter), "a"));
        Assertions.assertEquals(Map.of("a_00", 2, "a_01", 1), ComparableTableMappingContextTest.load(
                DataSetTestSupport.separators(split.replace("JOIN", """
                        , {"innerJoin": {"left": "a_00", "right": "c", "column": ["key"]}}""")).filter(filter), "a"));
    }

    @Test
    public void joinの出力が出力の絞り込みに合わなければ_joinの元テーブルは読まない() {
        Assertions.assertEquals(Map.of("a", false, "b", false, "x", true), ComparableTableMappingContextTest.rowsRead("""
                {"outputFilter": {"name": "x"}
                , "settings": [{"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """, "a", "b", "x"));
        Assertions.assertEquals(Map.of("a", true, "b", true, "x", false), ComparableTableMappingContextTest.rowsRead("""
                {"outputFilter": {"name": "a_with_b"}
                , "settings": [{"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """, "a", "b", "x"));
    }

    @Test
    public void joinの結果を使うjoinの出力が出力の絞り込みに合えば_使われるjoinの元テーブルも読む() {
        Assertions.assertEquals(Map.of("a", true, "b", true, "c", true), ComparableTableMappingContextTest.rowsRead("""
                {"outputFilter": {"name": "a_with_b_with_c"}
                , "settings": [{"innerJoin": {"left": "a_with_b", "right": "c", "on": "a_with_b_a_key == c_key"}}
                            , {"innerJoin": {"left": "a", "right": "b", "column": ["key"]}}]}
                """, "a", "b", "c"));
    }

    @Test
    public void 分割するテーブルはjoinに使えず_実行しなかったjoinを警告する() {
        final Logger logger = (Logger) LoggerFactory.getLogger(ComparableTableMappingContext.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            for (final String left : List.of("a", "a_00")) {
                appender.list.clear();
                Assertions.assertEquals(Map.of("a_00", 2, "a_01", 1, "b", 3), ComparableTableMappingContextTest.load("""
                        {"settings": [{"name": "a", "split": {"suffix": "_%02d", "limit": 2}}
                                    , {"innerJoin": {"left": "LEFT", "right": "b", "column": ["key"]}}]}
                        """.replace("LEFT", left), "a", "b"));
                Assertions.assertEquals(List.of(Level.WARN), appender.list.stream().map(ILoggingEvent::getLevel).toList(), left);
            }
        } finally {
            logger.detachAppender(appender);
        }
    }

    /**
     * key列に1〜3の3行を持つテーブルtableNamesを読み込み、テーブルごとに行を読んだか（行が使われるか）を返す
     */
    private static Map<String, Boolean> rowsRead(final String setting, final String... tableNames) {
        final ComparableDataSetParam param = ComparableDataSetParam.builder()
                                                                   .setTableSeparators(DataSetTestSupport.separators(setting))
                                                                   .build();
        final Map<String, Boolean> result = new LinkedHashMap<>();
        final ComparableTableMappingContext context = new ComparableTableMappingContext(param.tableSeparators(), null);
        context.open();
        Stream.of(tableNames).forEach(tableName -> new DataSetTestSupport.KeysTask(tableName, param
                , (task, read) -> result.put(task.tableName(), read), "1", "2", "3").run(context));
        context.close();
        return result;
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
        return ComparableTableMappingContextTest.load(DataSetTestSupport.separators(setting), tableNames);
    }

    private static Map<String, Integer> load(final TableSeparators separators, final String... tableNames) {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(separators, null);
        context.open();
        Stream.of(tableNames).forEach(tableName -> DataSetTestSupport.write(context, tableName, "1", "2", "3"));
        final Map<String, Integer> result = new LinkedHashMap<>();
        context.close().forEach((tableName, table) -> result.put(tableName, table.getRowCount()));
        return result;
    }
}
