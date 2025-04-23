package yo.dbunitcli.application.json;

import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.poi.XlsxSchema;

import static org.junit.jupiter.api.Assertions.*;

class FromJsonXlsxSchemaBuilderTest {

    @Test
    void testBuildWithSheetPattern() {
        // JSON with sheet pattern definition
        final String json = """
                {
                    "patterns": {
                        "data": "data_",
                        "summary": "summary_"
                    },
                    "rows": [
                        {
                            "sheetName": "data",
                            "tableName": "DATA_TABLE",
                            "header": ["id", "name", "value"],
                            "columnIndex": [0, 1, 2],
                            "dataStart": 1,
                            "breakKey": []
                        },
                        {
                            "sheetName": "summary",
                            "tableName": "SUMMARY_TABLE",
                            "header": ["category", "total"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        }
                    ]
                }
                """;

        final FromJsonXlsxSchemaBuilder builder = new FromJsonXlsxSchemaBuilder();
        final XlsxSchema schema = builder.build(json);

        // Test pattern matching for data sheets
        assertTrue(schema.contains("data_001"));
        assertTrue(schema.contains("data_999"));
        assertTrue(schema.contains("data_abc")); // should match as it contains "data_"

        // Test pattern matching for summary sheets
        assertTrue(schema.contains("summary_2024"));
        assertTrue(schema.contains("summary_monthly"));
        assertFalse(schema.contains("summarysheet")); // no underscore after "summary"

        // Test builder creation for matched sheet
        assertNotEquals(XlsxSchema.DEFAULT, schema.getRowsTableBuilder("data_001", new String[]{}));
        assertNotEquals(XlsxSchema.DEFAULT, schema.getRowsTableBuilder("summary_2024", new String[]{}));
    }

    @Test
    void testBuildWithoutPattern() {
        final String json = """
                {
                    "rows": [
                        {
                            "sheetName": "Sheet1",
                            "tableName": "TEST_TABLE",
                            "header": ["id", "name"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        }
                    ]
                }
                """;

        final FromJsonXlsxSchemaBuilder builder = new FromJsonXlsxSchemaBuilder();
        final XlsxSchema schema = builder.build(json);

        // Test exact sheet name match
        assertTrue(schema.contains("Sheet1"));
        assertFalse(schema.contains("Sheet2"));
    }

    @Test
    void testBuildWithMixedPatternAndExactMatch() {
        final String json = """
                {
                    "patterns": {
                        "monthly": "monthly_"
                    },
                    "rows": [
                        {
                            "sheetName": "monthly",
                            "tableName": "MONTHLY_DATA",
                            "header": ["date", "amount"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        },
                        {
                            "sheetName": "summary",
                            "tableName": "SUMMARY",
                            "header": ["category", "total"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        }
                    ]
                }
                """;

        final FromJsonXlsxSchemaBuilder builder = new FromJsonXlsxSchemaBuilder();
        final XlsxSchema schema = builder.build(json);

        // Test pattern matching
        assertTrue(schema.contains("monthly_2024_01"));
        assertTrue(schema.contains("monthly_2024_12"));
        assertTrue(schema.contains("monthly_2024")); // should match as it contains "monthly_"

        // Test exact matching
        assertTrue(schema.contains("summary"));
    }

    @Test
    void testBuildWithContainsPattern() {
        final String json = """
                {
                    "patterns": {
                        "data": "data_"
                    },
                    "rows": [
                        {
                            "sheetName": "data",
                            "tableName": "DATA_TABLE",
                            "header": ["id", "name"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        }
                    ]
                }
                """;

        final FromJsonXlsxSchemaBuilder builder = new FromJsonXlsxSchemaBuilder();
        final XlsxSchema schema = builder.build(json);

        assertTrue(schema.contains("data_001"));
        assertTrue(schema.contains("data_abc"));
        assertTrue(schema.contains("other_data_"));
    }

    @Test
    void testBuildWithAnyPattern() {
        final String json = """
                {
                    "patterns": {
                        "data": ["table1", "table2"]
                    },
                    "rows": [
                        {
                            "sheetName": "data",
                            "tableName": "DATA_TABLE",
                            "header": ["id", "name"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        }
                    ]
                }
                """;

        final FromJsonXlsxSchemaBuilder builder = new FromJsonXlsxSchemaBuilder();
        final XlsxSchema schema = builder.build(json);

        // Test any pattern
        assertTrue(schema.contains("table1"));
        assertTrue(schema.contains("table2"));
        assertFalse(schema.contains("table3"));
    }

    @Test
    void testBuildWithContainsAndExcludePattern() {
        final String json = """
                {
                    "patterns": {
                        "data": {
                            "string": "data_",
                            "exclude": ["data_temp_001", "data_log_abc"]
                        }
                    },
                    "rows": [
                        {
                            "sheetName": "data",
                            "tableName": "DATA_TABLE",
                            "header": ["id", "name"],
                            "columnIndex": [0, 1],
                            "dataStart": 1,
                            "breakKey": []
                        }
                    ]
                }
                """;

        final FromJsonXlsxSchemaBuilder builder = new FromJsonXlsxSchemaBuilder();
        final XlsxSchema schema = builder.build(json);

        // Test contains with exclude pattern
        assertTrue(schema.contains("data_001"));
        assertTrue(schema.contains("data_abc"));
        assertFalse(schema.contains("data_temp_001"));
        assertFalse(schema.contains("data_log_abc"));
    }
}