package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.TableSeparators;
import yo.dbunitcli.resource.poi.XlsxSchema;

import java.io.File;
import java.util.Arrays;
import java.util.TreeMap;

public class XlsxSchemaHandlerTest {

    @Test
    public void loadDataがfalseならヘッダ行を読んだ時点でシートの解析を打ち切る() {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(TableSeparators.NONE, null);
        final XlsxSchemaHandler handler = this.handler(context, false);

        this.row(handler, 0, "key", "value");
        Assertions.assertThrows(XlsxSchemaHandler.StopSheet.class, () -> handler.endRow(0));
        handler.endSheet();

        final ComparableTable table = context.close().get("sheet");
        Assertions.assertArrayEquals(new String[]{"key", "value"}, Arrays.stream(table.getTableMetaData().getColumns())
                                                                         .map(Column::getColumnName)
                                                                         .toArray(String[]::new));
        Assertions.assertEquals(0, table.getRowCount());
    }

    @Test
    public void loadDataがtrueなら最後の行まで読む() {
        final ComparableTableMappingContext context = new ComparableTableMappingContext(TableSeparators.NONE, null);
        final XlsxSchemaHandler handler = this.handler(context, true);

        this.row(handler, 0, "key", "value");
        handler.endRow(0);
        this.row(handler, 1, "1", "a");
        handler.endRow(1);
        handler.endSheet();

        final TreeMap<String, ComparableTable> result = context.close();
        Assertions.assertEquals(1, result.get("sheet").getRowCount());
    }

    private XlsxSchemaHandler handler(final ComparableTableMappingContext context, final boolean loadData) {
        return new XlsxSchemaHandler(context, XlsxSchema.DEFAULT, 1, null, loadData
                , new Source(new File("book.xlsx"), false).sheetName("sheet"));
    }

    private void row(final XlsxSchemaHandler handler, final int rowNum, final String... values) {
        handler.startRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            handler.cell(String.valueOf((char) ('A' + i)) + (rowNum + 1), values[i], null);
        }
    }
}
