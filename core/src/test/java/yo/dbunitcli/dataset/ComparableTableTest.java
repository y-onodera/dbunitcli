package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.OrderedTableNameMap;
import org.dbunit.dataset.datatype.DataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ComparableTableTest {

    private ComparableTable target;

    private final DefaultTableMetaData table = new DefaultTableMetaData("TABLE1", new Column[]{
            new Column("COLUMN1", DataType.UNKNOWN)
            , new Column("COLUMN2", DataType.UNKNOWN)
            , new Column("COLUMN3", DataType.UNKNOWN)
            , new Column("", DataType.UNKNOWN)
    });

    @Before
    public void setUp() throws DataSetException {
        final ComparableTableMapper builder = TableSeparators.NONE.createMapper(this.table);
        builder.startTable(null, new HashMap<>());
        builder.addRow(new Object[]{"1", "a", "あ", 1});
        builder.addRow(new Object[]{"1", "b", "あ", 1});
        builder.addRow(new Object[]{"2", "a", "い", 2});
        final OrderedTableNameMap map = new OrderedTableNameMap(true);
        builder.endTable(map);
        this.target = (ComparableTable) map.orderedValues().iterator().next();
    }

    @Test
    public void getRows_result_has_argument_column_length() {
        final Collection<Map.Entry<Integer, Object[]>> actual = this.target.getRows(List.of("COLUMN1", "COLUMN2")).values();
        final Iterator<Map.Entry<Integer, Object[]>> it = actual.iterator();
        assertArrayEquals(new Object[]{"1", "a", "あ", 1}, it.next().getValue());
        assertArrayEquals(new Object[]{"1", "b", "あ", 1}, it.next().getValue());
        assertArrayEquals(new Object[]{"2", "a", "い", 2}, it.next().getValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void getRows_throw_error_if_keys_not_unique() {
        Assert.assertThrows(AssertionError.class,
                () -> this.target.getRows(List.of("COLUMN1")).values()
        );
    }

    @Test
    public void getRows_throw_exception_if_argument_column_not_exists() {
        Assert.assertThrows(AssertionError.class,
                () -> this.target.getRows(List.of("COLUMN1", "COLUMNA")).values()
        );
    }

    @Test
    public void getKey_return_argument_column() {
        CompareKeys actual = this.target.getKey(0, List.of("COLUMN1", "COLUMN2"));
        assertEquals(new CompareKeys(0, List.of("1", "a")), actual);
        actual = this.target.getKey(1, List.of("COLUMN1", "COLUMN2"));
        assertEquals(new CompareKeys(1, List.of("1", "b")), actual);
        actual = this.target.getKey(2, List.of("COLUMN1", "COLUMN2"));
        assertEquals(new CompareKeys(2, List.of("2", "a")), actual);
    }

    @Test
    public void getRow_result_has_argument_column_length() {
        assertArrayEquals(new Object[]{"1", "a", "あ"}, this.target.getRow(0, 3));
        assertArrayEquals(new Object[]{"1", "b", "あ"}, this.target.getRow(1, 3));
        assertArrayEquals(new Object[]{"2", "a", "い"}, this.target.getRow(2, 3));
    }

    @Test
    public void getRowCount_return_delegateTableRowCount() {
        assertEquals(3, this.target.getRowCount());
    }

    @Test
    public void getValue_return_argument_row_and_column_value() throws DataSetException {
        assertEquals("2", this.target.getValue(2, "COLUMN1"));
        assertEquals("a", this.target.getValue(0, "COLUMN2"));
        assertEquals("b", this.target.getValue(1, "COLUMN2"));
        assertEquals("a", this.target.getValue(2, "COLUMN2"));
    }

    @Test
    public void getValue_return_argument_row_and_column_length_value() {
        assertEquals("2", this.target.getValue(2, 0));
        assertEquals("a", this.target.getValue(0, 1));
        assertEquals("b", this.target.getValue(1, 1));
        assertEquals("a", this.target.getValue(2, 1));
        assertEquals(2, this.target.getValue(2, 3));
    }

}