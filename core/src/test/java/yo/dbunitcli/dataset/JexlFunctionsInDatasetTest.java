package yo.dbunitcli.dataset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JexlFunctionsInDatasetTest {

    @Test
    void numberColumnAcceptsTryToNum() {
        final ExpressionColumns columns = ExpressionColumns.builder()
                .addExpressions(new ExpressionColumns.Expressions(ExpressionColumns.ParameterType.NUMBER
                        , new HashMap<>(Map.of("num", "tryToNum(a, 0)"))))
                .build();
        Assertions.assertEquals(new BigDecimal("1234"), columns.evaluate("num", Map.of("a", "1,234")));
        Assertions.assertEquals(new BigDecimal("0"), columns.evaluate("num", Map.of("a", "abc")));
    }

    @Test
    void filterAcceptsTryToNum() {
        final TableSeparator.RowFilter filter = TableSeparator.RowFilter.of(List.of("tryToNum(a, -1) > 1000"));
        Assertions.assertTrue(filter.test(Map.of("a", "1,234")));
        Assertions.assertFalse(filter.test(Map.of("a", "abc")));
    }

    @Test
    void nullを返す式でも追加カラムとfilterを評価できる() {
        final Map<String, Object> row = new HashMap<>();
        row.put("a", null);
        final ExpressionColumns columns = ExpressionColumns.builder()
                .addExpressions(new ExpressionColumns.Expressions(ExpressionColumns.ParameterType.NUMBER
                        , new HashMap<>(Map.of("num", "a"))))
                .addExpressions(new ExpressionColumns.Expressions(ExpressionColumns.ParameterType.BOOLEAN
                        , new HashMap<>(Map.of("bool", "a"))))
                .addExpressions(new ExpressionColumns.Expressions(ExpressionColumns.ParameterType.SQL_FUNCTION
                        , new HashMap<>(Map.of("func", "a"))))
                .build();
        Assertions.assertNull(columns.evaluate("num", row));
        Assertions.assertEquals(false, columns.evaluate("bool", row));
        Assertions.assertNull(columns.evaluate("func", row));
        Assertions.assertFalse(TableSeparator.RowFilter.of(List.of("a")).test(row));
    }
}
