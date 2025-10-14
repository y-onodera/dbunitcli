package yo.dbunitcli.dataset.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import yo.dbunitcli.common.TableMetaDataFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TableMetaDataFilter}のテストクラス。
 * テーブル名やシート名に対するフィルタリング機能のテストを実施します。
 */
public class SourceFilterTest {

    private static final String TABLE1 = "table1";
    private static final String TABLE2 = "table2";
    private static final String TABLE3 = "table3";
    private static final String USER_INFO = "user_info";
    private static final String USER_ADMIN = "user_admin";
    private static final String CUSTOMER = "customer";

    @BeforeEach
    protected void setUp() {
        // 将来の拡張に備えて用意
    }

    @Test
    public void regexWhenValidPatternShouldMatchTables() {
        final var filter = new RegexFilter("table[0-9]+");
        assertTrue(filter.test(TABLE1));
        assertTrue(filter.test(TABLE2));
        assertFalse(filter.test(USER_INFO));
    }

    @Test
    public void anyWhenExactMatchShouldReturnTrue() {
        final var filter = new AnyFilter(List.of(TABLE1, TABLE2));
        assertTrue(filter.test(TABLE1));
        assertTrue(filter.test(TABLE2));
        assertFalse(filter.test(TABLE3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user_info", "user"})
    public void containWhenPatternExistsInNameShouldReturnTrue(final String tableName) {
        final var filter = new ContainFilter(List.of("user"));
        assertTrue(filter.test(tableName));
    }

    @Test
    public void containWhenPatternNotExistsInNameShouldReturnFalse() {
        final var filter = new ContainFilter(List.of("user"));
        assertFalse(filter.test(CUSTOMER));
    }

    @Test
    public void containWhenAsteriskPatternShouldAlwaysReturnTrue() {
        final var filter = new ContainFilter(List.of("*"));
        assertTrue(filter.test("any_table"));
        assertTrue(filter.test(""));
    }

    @Test
    public void excludeWhenNameInExcludeListShouldReturnFalse() {
        final var filter = new ContainFilter(List.of("user")).exclude(List.of(USER_ADMIN));
        assertTrue(filter.test(USER_INFO));
        assertFalse(filter.test(USER_ADMIN));
        assertFalse(filter.test(CUSTOMER));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void alwaysWhenCalledShouldReturnSpecifiedValue(final boolean expected) {
        assertEquals(expected, new AlwaysFilter(expected).test("any"));
    }
}
