package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.TableSeparator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RowCompareTest {

    private static final TableSeparator KEY_ID = TableSeparator.builder().setComparisonKeys(List.of("ID")).build();

    private static ComparableTable table(final String[] columns, final Object[]... rows) {
        final ComparableTable.Builder builder = new ComparableTable.Builder(KEY_ID.addSetting(new DefaultTableMetaData("T",
                java.util.Arrays.stream(columns).map(it -> new Column(it, DataType.UNKNOWN)).toArray(Column[]::new))));
        for (final Object[] row : rows) {
            builder.addRow(row);
        }
        return builder.build();
    }

    private static List<CompareDiff> compare(final ComparableTable oldTable, final ComparableTable newTable) {
        final TableCompare tableCompare = new TableCompare(oldTable, newTable, null);
        return new DefaultCompareManager.RowCompare(tableCompare, new DataRowCompareResultHandler(tableCompare)).exec();
    }

    @Test
    public void oldの比較キーが重複していれば失敗する() {
        final ComparableTable oldTable = table(new String[]{"ID", "V"}, new Object[]{"1", "a"}, new Object[]{"1", "b"});
        final ComparableTable newTable = table(new String[]{"ID", "V"}, new Object[]{"1", "a"});
        final AssertionError error = assertThrows(AssertionError.class, () -> compare(oldTable, newTable));
        assertTrue(error.getMessage().startsWith("comparison keys not unique:[ID]"), error.getMessage());
    }

    @Test
    public void newに比較キーの列が無ければ不足した列名を示して失敗する() {
        final ComparableTable oldTable = table(new String[]{"ID", "V"}, new Object[]{"1", "a"});
        final ComparableTable newTable = table(new String[]{"ID2", "V"}, new Object[]{"1", "a"});
        final AssertionError error = assertThrows(AssertionError.class, () -> compare(oldTable, newTable));
        assertTrue(error.getMessage().startsWith("comparison keys not found in new table:[ID]"), error.getMessage());
    }

    @Test
    public void パッチSQLの主キーは比較キーと大文字小文字を区別せずに照合する() {
        final TableSeparator lowerKey = TableSeparator.builder().setComparisonKeys(List.of("id")).build();
        final ComparableTable oldTable = new ComparableTable.Builder(lowerKey.addSetting(new DefaultTableMetaData("T"
                , new Column[]{new Column("ID", DataType.UNKNOWN), new Column("V", DataType.UNKNOWN)})))
                .addRow(new Object[]{"1", "a"})
                .build();
        final boolean[] written = new boolean[1];
        final CreatePatchSqlRowCompareResultHandler handler = new CreatePatchSqlRowCompareResultHandler(
                new TableCompare(oldTable, oldTable, null)) {
            @Override
            protected void write() {
                written[0] = true;
            }
        };
        handler.handleDelete(0, oldTable.getRow(0));
        handler.result();
        assertTrue(written[0]);
    }
}
