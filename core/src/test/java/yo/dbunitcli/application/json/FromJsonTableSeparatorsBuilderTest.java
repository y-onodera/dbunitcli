package yo.dbunitcli.application.json;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.TableSeparator;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class FromJsonTableSeparatorsBuilderTest {

    @Test
    public void configureSettingWhenRegexPatternShouldMatchTableName() {
        final String json = """
                {
                    "settings": [{
                        "pattern": {
                            "regex": "^DATA_\\\\d+$"
                        },
                        "keys": ["id"]
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();
        assertTrue(tableSeparators.hasAdditionalSetting(new DefaultTableMetaData("DATA_123", new Column[0])), "Should match numeric pattern");
        assertFalse(tableSeparators.hasAdditionalSetting(new DefaultTableMetaData("DATA_ABC", new Column[0])), "Should not match non-numeric pattern");
    }

    @Test
    public void configureSettingWhenMultiplePatternShouldMatchAllPatterns() {
        final String json = """
                {
                    "settings": [{
                        "name": "FIXED_TABLE",
                        "keys": ["id"]
                    }, {
                        "pattern": {
                            "regex": "^DYNAMIC_\\\\d+$"
                        },
                        "keys": ["id"]
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();

        assertTrue(tableSeparators.hasAdditionalSetting(new DefaultTableMetaData("FIXED_TABLE", new Column[0])), "Should match fixed table name");
        assertTrue(tableSeparators.hasAdditionalSetting(new DefaultTableMetaData("DYNAMIC_123", new Column[0])), "Should match dynamic pattern");
        assertFalse(tableSeparators.hasAdditionalSetting(new DefaultTableMetaData("OTHER_TABLE", new Column[0])), "Should not match other table names");
    }

    @Test
    public void configureSettingWhenInnerJoinShouldCreateJoinCondition() {
        final String json = """
                {
                    "settings": [{
                        "innerJoin": {
                            "left": "TABLE1",
                            "right": "TABLE2",
                            "column": ["id", "code"]
                        }
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();

        assertFalse(tableSeparators.joinConditions().isEmpty(), "Should have join conditions");
        assertEquals("TABLE1", tableSeparators.joinConditions().getFirst().left(), "Should have correct left table");
        assertEquals("TABLE2", tableSeparators.joinConditions().getFirst().right(), "Should have correct right table");
        assertEquals("InnerJoin", tableSeparators.joinConditions().getFirst().strategy().getClass().getSimpleName(),
                "Should have inner join strategy");
    }

    @Test
    public void configureSettingWhenJoinHasFilePathShouldMatchOnlyTablesFromTheFile() {
        final String json = """
                {
                    "settings": [{
                        "innerJoin": {
                            "left": "TABLE1",
                            "filePath": ".*/a/.*",
                            "right": "TABLE2",
                            "rightFilePath": ".*/b/.*",
                            "column": ["id"]
                        }
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final var condition = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build()
                .joinConditions().getFirst();

        assertTrue(condition.isLeft(this.metaData("TABLE1", "/work/a/TABLE1.csv")), "left table from left file path");
        assertFalse(condition.isLeft(this.metaData("TABLE1", "/work/b/TABLE1.csv")), "left table from other file path");
        assertTrue(condition.isRight(this.metaData("TABLE2", "/work/b/TABLE2.csv")), "right table from right file path");
        assertFalse(condition.isRight(this.metaData("TABLE2", "/work/a/TABLE2.csv")), "right table from other file path");
        assertFalse(condition.hasRelation(this.metaData("TABLE3", "/work/a/TABLE3.csv")), "unrelated table");
    }

    @Test
    public void configureSettingWhenJoinHasNoFilePathShouldMatchByTableNameOnly() {
        final String json = """
                {
                    "settings": [{
                        "innerJoin": {
                            "left": "TABLE1",
                            "right": "TABLE2",
                            "column": ["id"]
                        }
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final var condition = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build()
                .joinConditions().getFirst();

        assertTrue(condition.isLeft(this.metaData("TABLE1", "/work/a/TABLE1.csv")));
        assertTrue(condition.isRight(this.metaData("TABLE2", "/work/b/TABLE2.csv")));
    }

    @Test
    public void configureSettingWhenJoinHasCommonFilePathShouldApplyToSidesWithoutOwnFilePath() {
        final String json = """
                {
                    "settings": [{
                        "innerJoin": {
                            "left": "TABLE1",
                            "right": "TABLE2",
                            "filePath": ".*/a/.*",
                            "rightFilePath": ".*/b/.*",
                            "column": ["id"]
                        }
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final var condition = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build()
                .joinConditions().getFirst();

        assertTrue(condition.isLeft(this.metaData("TABLE1", "/work/a/TABLE1.csv")), "left falls back to filePath");
        assertFalse(condition.isLeft(this.metaData("TABLE1", "/work/b/TABLE1.csv")));
        assertTrue(condition.isRight(this.metaData("TABLE2", "/work/b/TABLE2.csv")), "rightFilePath wins over filePath");
        assertFalse(condition.isRight(this.metaData("TABLE2", "/work/a/TABLE2.csv")));
    }

    private AddSettingTableMetaData metaData(final String tableName, final String filePath) {
        return TableSeparator.NONE.addSetting(new Source(filePath, "", "", "", false, false)
                .wrap(new DefaultTableMetaData(tableName, new Column[0])));
    }

    @Test
    public void configureSettingWhenSplitConfiguredShouldSetupTableSplitter() {
        final String json = """
                {
                    "settings": [{
                        "name": "LARGE_TABLE",
                        "split": {
                            "limit": 1000,
                            "prefix": "SPLIT_",
                            "suffix": "_DATA"
                        },
                        "keys": ["id"]
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();

        assertTrue(tableSeparators.hasAdditionalSetting(new DefaultTableMetaData("LARGE_TABLE", new Column[0])), "Should match table name");
        final var separator = tableSeparators.settings().getFirst();
        assertNotNull(separator, "Should have separator configuration");
        assertEquals(1000, separator.splitter().limit(), "Should have correct split limit");
        assertTrue(separator.splitter().isSplit(), "Should be split configuration");

        // テーブル名の変換をテスト
        final String renamedTable = separator.splitter().renameFunction().apply("LARGE_TABLE", 1);
        assertTrue(renamedTable.startsWith("SPLIT_"), "Should have correct prefix");
        assertTrue(renamedTable.endsWith("_DATA"), "Should have correct suffix");
    }

    @Test
    public void configureSettingWhenColumnFilterConfiguredShouldSetupFilters() {
        final String json = """
                {
                    "settings": [{
                        "name": "TEST_TABLE",
                        "keys": ["id"],
                        "include": ["col1", "col2"],
                        "exclude": ["temp_col"],
                        "order": ["id", "col1"]
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();

        final var separator = tableSeparators.settings().getFirst();
        assertNotNull(separator, "Should have table setting");
        assertTrue(separator.includeColumns().contains("col1"), "Should have included column");
        assertTrue(separator.excludeColumns().contains("temp_col"), "Should have excluded column");
        assertEquals("id", separator.orderColumns().getFirst(), "Should have correct order column");
    }

    @Test
    public void configureSettingWhenDistinctAndExpressionConfiguredShouldSetupBoth() {
        final String json = """
                {
                    "settings": [{
                        "name": "TEST_TABLE",
                        "keys": ["id"],
                        "distinct": true,
                        "string": {
                            "calculated_col": "CONCAT(col1, col2)"
                        },
                        "number": {
                            "sum_col": "value1 + value2"
                        }
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();

        final var separator = tableSeparators.settings().getFirst();
        assertNotNull(separator, "Should have table setting");
        assertTrue(separator.distinct(), "Should be distinct");
        assertTrue(separator.expressionColumns().size() > 0, "Should have expression columns");
        assertTrue(separator.expressionColumns().contains("calculated_col"), "Should have string expression");
        assertTrue(separator.expressionColumns().contains("sum_col"), "Should have number expression");
    }

    @Test
    public void configureSettingWhenFilterConfiguredShouldSetupRowFilters() {
        final String json = """
                {
                    "settings": [{
                        "name": "TEST_TABLE",
                        "keys": ["id"],
                        "filter": [
                            "value > 0",
                            "status != 'DELETED'"
                        ]
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jsonObject)
                .build();

        final var separator = tableSeparators.settings().getFirst();
        assertNotNull(separator, "Should have table setting");
        assertFalse(separator.filter().expressions().isEmpty(), "Should have filter expressions");
        assertEquals(2, separator.filter().expressions().size(), "Should have correct number of filter expressions");
    }

    @Test
    public void configureCommonSettingWhenConfiguredShouldApplyToAllTables() {
        final String json = """
                {
                    "commonSettings": [{
                        "include": ["common_col1", "common_col2"],
                        "exclude": ["temp_*"],
                        "order": ["id"]
                    }]
                }
                """;

        final jakarta.json.JsonObject jsonObject = jakarta.json.Json.createReader(new StringReader(json))
                .readObject();
        final TableSeparators tableSeparators = new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureCommonSetting(jsonObject)
                .build();

        assertFalse(tableSeparators.commonSettings().isEmpty(), "Should have common settings");
        final var commonSetting = tableSeparators.commonSettings().getFirst();
        assertTrue(commonSetting.includeColumns().contains("common_col1"), "Should have common included column");
        assertTrue(commonSetting.excludeColumns().contains("temp_*"), "Should have common excluded column");
        assertEquals("id", commonSetting.orderColumns().getFirst(), "Should have common order column");
        assertTrue(commonSetting.sourceFilter().test("ANY_TABLE"), "Should match any table name");
    }

    private static TableSeparators fromJson(final String json) {
        return new FromJsonTableSeparatorsBuilder("UTF-8")
                .configureSetting(jakarta.json.Json.createReader(new StringReader(json)).readObject())
                .configureOutputFilter(jakarta.json.Json.createReader(new StringReader(json)).readObject())
                .build();
    }

    private static long outputCount(final TableSeparators separators, final String tableName) {
        return separators.getAddSettingTableMetaData(new DefaultTableMetaData(tableName, new Column[0])).count();
    }

    @Test
    public void outputFilterWhenNotConfiguredShouldOutputAllTables() {
        final TableSeparators separators = fromJson("{\"settings\": [{\"name\": \"A\", \"keys\": [\"id\"]}]}");
        assertEquals(1, outputCount(separators, "A"));
        assertEquals(1, outputCount(separators, "B"));
    }

    @Test
    public void outputFilterWhenNameShouldOutputOnlyMatchedTables() {
        final TableSeparators separators = fromJson("{\"outputFilter\": {\"name\": [\"A\", \"B\"]}}");
        assertEquals(1, outputCount(separators, "A"));
        assertEquals(1, outputCount(separators, "B"));
        assertEquals(0, outputCount(separators, "C"));
    }

    @Test
    public void outputFilterWhenPatternShouldSupportRegexAndExclude() {
        final TableSeparators separators = fromJson("{\"outputFilter\": {\"pattern\": {\"regex\": \"^DATA_\\\\d+$\", \"exclude\": [\"DATA_2\"]}}}");
        assertEquals(1, outputCount(separators, "DATA_1"));
        assertEquals(0, outputCount(separators, "DATA_2"));
        assertEquals(0, outputCount(separators, "OTHER"));
    }

    @Test
    public void outputFilterShouldBeAppliedToRenamedTable() {
        final TableSeparators separators = fromJson("""
                {"settings": [{"name": "A", "tableName": "NEW_A"}],
                 "outputFilter": {"name": "NEW_A"}}
                """);
        assertEquals(1, outputCount(separators, "A"));
    }

    @Test
    public void outputFilterWhenInvalidShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> fromJson("{\"outputFilter\": {}}"));
        assertThrows(IllegalArgumentException.class, () -> fromJson("{\"outputFilter\": {\"name\": {\"any\": \"A\", \"filePath\": \".*\"}}}"));
    }

    @Test
    public void outputFilterOfImportShouldBeCombinedWithAndByDefaultAndOrWhenSpecified(@org.junit.jupiter.api.io.TempDir final java.nio.file.Path dir) throws java.io.IOException {
        java.nio.file.Files.writeString(dir.resolve("child.json"), "{\"outputFilter\": {\"name\": [\"A\", \"B\"]}}");
        java.nio.file.Files.writeString(dir.resolve("and.json"), "{\"outputFilter\": {\"name\": [\"B\", \"C\"]}, \"import\": [{\"path\": \"child.json\"}]}");
        java.nio.file.Files.writeString(dir.resolve("or.json"), "{\"outputFilter\": {\"name\": [\"B\", \"C\"]}, \"import\": [{\"path\": \"child.json\", \"outputFilterCombine\": \"or\"}]}");

        final TableSeparators and = new FromJsonTableSeparatorsBuilder("UTF-8").build(dir.resolve("and.json").toFile());
        assertEquals(0, outputCount(and, "A"));
        assertEquals(1, outputCount(and, "B"));
        assertEquals(0, outputCount(and, "C"));

        final TableSeparators or = new FromJsonTableSeparatorsBuilder("UTF-8").build(dir.resolve("or.json").toFile());
        assertEquals(1, outputCount(or, "A"));
        assertEquals(1, outputCount(or, "B"));
        assertEquals(1, outputCount(or, "C"));
        assertEquals(0, outputCount(or, "D"));
    }
}
