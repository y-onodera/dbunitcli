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
}
