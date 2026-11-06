package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.MapContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class JexlFunctionsTest {

    private static Object eval(final String expression, final Map<String, Object> param) {
        return JexlFunctions.ENGINE.createExpression(expression).evaluate(new MapContext(param));
    }

    @Test
    void tryToNumReturnsBigDecimalWhenConvertible() {
        Assertions.assertEquals(new BigDecimal("1234567.89"), eval("tryToNum(a, 0)", Map.of("a", "1,234,567.89")));
        Assertions.assertEquals(new BigDecimal("-10"), eval("tryToNum(a, 0)", Map.of("a", "-10")));
        Assertions.assertEquals(new BigDecimal("5"), eval("tryToNum(a, 0)", Map.of("a", 5)));
    }

    @Test
    void tryToNumReturnsDefaultWhenNotConvertible() {
        Assertions.assertEquals("N/A", eval("tryToNum(a, 'N/A')", Map.of("a", "12a")));
        Assertions.assertEquals("N/A", eval("tryToNum(a, 'N/A')", Map.of("a", "")));
        final Map<String, Object> nullParam = new HashMap<>();
        nullParam.put("a", null);
        Assertions.assertEquals(0, eval("tryToNum(a, 0)", nullParam));
    }

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
        final TableSeparator.RowFilter filter = TableSeparator.RowFilter.of(java.util.List.of("tryToNum(a, -1) > 1000"));
        Assertions.assertTrue(filter.test(Map.of("a", "1,234")));
        Assertions.assertFalse(filter.test(Map.of("a", "abc")));
    }
}
