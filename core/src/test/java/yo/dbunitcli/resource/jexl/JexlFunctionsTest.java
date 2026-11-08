package yo.dbunitcli.resource.jexl;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

class JexlFunctionsTest {

    static Stream<JexlEngine> engines() {
        return Stream.of(JexlEngines.DATASET, JexlEngines.TEMPLATE);
    }

    private static Object eval(final JexlEngine engine, final String expression, final Map<String, Object> param) {
        return engine.createExpression(expression).evaluate(new MapContext(param));
    }

    @ParameterizedTest
    @MethodSource("engines")
    void tryToNumReturnsBigDecimalWhenConvertible(final JexlEngine engine) {
        Assertions.assertEquals(new BigDecimal("1234567.89"), eval(engine, "tryToNum(a, 0)", Map.of("a", "1,234,567.89")));
        Assertions.assertEquals(new BigDecimal("-10"), eval(engine, "tryToNum(a, 0)", Map.of("a", "-10")));
        Assertions.assertEquals(new BigDecimal("5"), eval(engine, "tryToNum(a, 0)", Map.of("a", 5)));
    }

    @ParameterizedTest
    @MethodSource("engines")
    void tryToNumReturnsDefaultWhenNotConvertible(final JexlEngine engine) {
        Assertions.assertEquals("N/A", eval(engine, "tryToNum(a, 'N/A')", Map.of("a", "12a")));
        Assertions.assertEquals("N/A", eval(engine, "tryToNum(a, 'N/A')", Map.of("a", "")));
        Assertions.assertEquals(0, eval(engine, "tryToNum(a, 0)", Collections.singletonMap("a", null)));
    }

    @ParameterizedTest
    @MethodSource("engines")
    void formatDateConvertsPattern(final JexlEngine engine) {
        Assertions.assertEquals("2026/11/06", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd')", Map.of("a", "20261106")));
        Assertions.assertEquals("2026/01/06", eval(engine, "formatDate(a, 'yyyy/M/d', 'yyyy/MM/dd')", Map.of("a", "2026/1/6")));
        Assertions.assertEquals("2026-11-06 23:05", eval(engine, "formatDate(a, 'yyyyMMddHHmmss', 'yyyy-MM-dd HH:mm')", Map.of("a", "20261106230512")));
    }

    @ParameterizedTest
    @MethodSource("engines")
    void formatDateReturnsValueWhenNotParsable(final JexlEngine engine) {
        Assertions.assertEquals("abc", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd')", Map.of("a", "abc")));
        Assertions.assertEquals("", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd')", Map.of("a", "")));
        Assertions.assertNull(eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd')", Collections.singletonMap("a", null)));
    }

    @ParameterizedTest
    @MethodSource("engines")
    void formatDateReturnsDefaultWhenNotParsable(final JexlEngine engine) {
        Assertions.assertEquals("2026/11/06", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd', '-')", Map.of("a", "20261106")));
        Assertions.assertEquals("-", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd', '-')", Map.of("a", "2026-11-06")));
        Assertions.assertEquals("-", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd', '-')", Map.of("a", "20260230")));
        Assertions.assertEquals("2024/02/29", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd', '-')", Map.of("a", "20240229")));
        Assertions.assertEquals("-", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd', '-')", Collections.singletonMap("a", null)));
    }

    @ParameterizedTest
    @MethodSource("engines")
    void internalHelperIsNotCallableFromExpression(final JexlEngine engine) {
        if (engine == JexlEngines.TEMPLATE) {
            // silent のため例外にならず null になる
            Assertions.assertNull(eval(engine, "parseOrNull(a)", Map.of("a", "1")));
        } else {
            Assertions.assertThrows(JexlException.class, () -> eval(engine, "parseOrNull(a)", Map.of("a", "1")));
        }
    }

    @ParameterizedTest
    @MethodSource("engines")
    void formatDateReturnsDefaultWhenNotFormattable(final JexlEngine engine) {
        Assertions.assertEquals("-", eval(engine, "formatDate(a, 'yyyyMMdd', 'yyyy/MM/dd HH:mm', '-')", Map.of("a", "20261106")));
    }
}
