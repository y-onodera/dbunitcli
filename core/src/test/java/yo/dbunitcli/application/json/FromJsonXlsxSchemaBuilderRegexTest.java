package yo.dbunitcli.application.json;

import org.junit.jupiter.api.Test;
import yo.dbunitcli.resource.poi.XlsxSchema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FromJsonXlsxSchemaBuilderRegexTest {

    @Test
    public void testRegexPatternInXlsxSchema() {
        final String json = """
                {
                    "patterns": {
                        "tableA": {
                            "regex": "^TABLE_[A-Z]+$"
                        }
                    }
                }
                """;

        final XlsxSchema schema = new FromJsonXlsxSchemaBuilder()
                .build(json);

        assertTrue(schema.contains("TABLE_ABC"), "Should match alphabetic pattern");
        assertFalse(schema.contains("TABLE_123"), "Should not match numeric pattern");
    }

    @Test
    public void testMultiplePatternWithRegexInXlsxSchema() {
        final String json = """
                {
                    "patterns": {
                        "fixedTable": {
                            "string": "FIXED_TABLE"
                        },
                        "dynamicTable": {
                            "regex": "^DYNAMIC_\\\\d+$"
                        }
                    }
                }
                """;

        final XlsxSchema schema = new FromJsonXlsxSchemaBuilder()
                .build(json);

        assertTrue(schema.contains("FIXED_TABLE"), "Should match fixed table name");
        assertTrue(schema.contains("DYNAMIC_123"), "Should match dynamic numeric pattern");
        assertFalse(schema.contains("DYNAMIC_ABC"), "Should not match dynamic non-numeric pattern");
    }
}