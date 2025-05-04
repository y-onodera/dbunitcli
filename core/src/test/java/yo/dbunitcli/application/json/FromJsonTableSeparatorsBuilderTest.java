package yo.dbunitcli.application.json;

import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.dataset.ComparableTableJoin;
import yo.dbunitcli.dataset.TableSeparator;

import java.io.StringReader;
import java.util.Collection;

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
        assertTrue(tableSeparators.hasAdditionalSetting("DATA_123"), "Should match numeric pattern");
        assertFalse(tableSeparators.hasAdditionalSetting("DATA_ABC"), "Should not match non-numeric pattern");
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

        assertTrue(tableSeparators.hasAdditionalSetting("FIXED_TABLE"), "Should match fixed table name");
        assertTrue(tableSeparators.hasAdditionalSetting("DYNAMIC_123"), "Should match dynamic pattern");
        assertFalse(tableSeparators.hasAdditionalSetting("OTHER_TABLE"), "Should not match other table names");
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

        assertFalse(tableSeparators.joins().isEmpty(), "Should have join conditions");
        assertEquals("TABLE1", tableSeparators.joins().get(0).left(), "Should have correct left table");
        assertEquals("TABLE2", tableSeparators.joins().get(0).right(), "Should have correct right table");
        assertEquals("InnerJoin",tableSeparators.joins().get(0).strategy().getClass().getSimpleName(), 
                "Should have inner join strategy");
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

        assertTrue(tableSeparators.hasAdditionalSetting("LARGE_TABLE"), "Should match table name");
        var separator = tableSeparators.settings().get(0);
        assertNotNull(separator, "Should have separator configuration");
        assertEquals(1000, separator.splitter().limit(), "Should have correct split limit");
        assertTrue(separator.splitter().isSplit(), "Should be split configuration");

        // テーブル名の変換をテスト
        String renamedTable = separator.splitter().renameFunction().apply("LARGE_TABLE", 1);
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

        var separator = tableSeparators.settings().get(0);
        assertNotNull(separator, "Should have table setting");
        assertTrue(separator.includeColumns().contains("col1"), "Should have included column");
        assertTrue(separator.excludeColumns().contains("temp_col"), "Should have excluded column");
        assertEquals("id", separator.orderColumns().get(0), "Should have correct order column");
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

        var separator = tableSeparators.settings().get(0);
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

        var separator = tableSeparators.settings().get(0);
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
        var commonSetting = tableSeparators.commonSettings().get(0);
        assertTrue(commonSetting.includeColumns().contains("common_col1"), "Should have common included column");
        assertTrue(commonSetting.excludeColumns().contains("temp_*"), "Should have common excluded column");
        assertEquals("id", commonSetting.orderColumns().get(0), "Should have common order column");
        assertTrue(commonSetting.targetFilter().test("ANY_TABLE"), "Should match any table name");
    }
}