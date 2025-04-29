package yo.dbunitcli.common.filter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetFilterTest {

    @Test
    void any_完全一致の場合にtrueを返す() {
        final var filter = TargetFilter.any("table1", "table2");
        assertTrue(filter.test("table1"));
        assertTrue(filter.test("table2"));
        assertFalse(filter.test("table3"));
    }

    @Test
    void contain_パターンを含む場合にtrueを返す() {
        final var filter = TargetFilter.contain("user");
        assertTrue(filter.test("user_info"));
        assertTrue(filter.test("user"));
        assertFalse(filter.test("customer"));
    }

    @Test
    void contain_アスタリスクの場合は常にtrueを返す() {
        final var filter = TargetFilter.contain("*");
        assertTrue(filter.test("any_table"));
        assertTrue(filter.test(""));
    }

    @Test
    void exclude_除外リストに含まれる場合にfalseを返す() {
        final var filter = TargetFilter.contain("user").exclude(List.of("user_admin"));
        assertTrue(filter.test("user_info"));
        assertFalse(filter.test("user_admin"));
        assertFalse(filter.test("customer"));
    }

    @Test
    void always_指定された値を常に返す() {
        assertTrue(TargetFilter.always(true).test("any"));
        assertFalse(TargetFilter.always(false).test("any"));
    }
}