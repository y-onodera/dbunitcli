package yo.dbunitcli.common.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class RegexFilterTest {

    @ParameterizedTest
    @CsvSource({
            "test_table,   test_table,      true",  // 完全一致
            "test_.*,      test_table,      true",  // ワイルドカード
            "test_\\d+,    test_123,        true",  // 数字のパターン
            ".*_table,     other_table,     true",  // プレフィックス任意
            "test_table,   other_table,     false", // 不一致
            "[a-z]+_\\d+,  test_123,        true",  // 複雑なパターン
            "test_table,   TEST_TABLE,      false"  // 大文字小文字の区別
    })
    public void patternMatching(final String pattern, final String input, final boolean expected) {
        // when
        final var filter = new RegexFilter(pattern);

        // then
        assertEquals(expected, filter.test(input));
    }

    @Test
    public void nullInput() {
        // given
        final var filter = new RegexFilter("test_.*");

        // when & then
        assertFalse(filter.test(null));
    }

    @Test
    public void nullPattern() {
        // when & then
        assertThrows(NullPointerException.class, () -> new RegexFilter((String) null));
    }

    @Test
    public void invalidPattern() {
        // when & then
        assertThrows(java.util.regex.PatternSyntaxException.class, () -> new RegexFilter("[invalid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    public void emptyOrBlankPattern(final String pattern) {
        // when
        final var filter = new RegexFilter(pattern);

        // then
        assertFalse(filter.test("table"));
    }
}