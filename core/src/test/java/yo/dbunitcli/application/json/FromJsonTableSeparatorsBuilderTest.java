package yo.dbunitcli.application.json;

import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FromJsonTableSeparatorsBuilderTest {

    @Test
    public void testRegexPatternInTableSeparators() {
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
    public void testMultiplePatternWithRegex() {
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
}