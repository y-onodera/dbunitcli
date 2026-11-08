package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;

import java.util.List;
import java.util.stream.Stream;

class ManualRowsMappingTableBuilderTest {

    @Test
    void currentRowFollowsColumnIndexOrder() {
        final XlsxRowsTableDefine def = XlsxRowsTableDefine.builder()
                .setTableName("TABLE")
                .setHeader(Stream.of("NAME", "ID"))
                .setDataStartRow(1)
                .addCellIndexes(Stream.of(3, 1))
                .addBreakKey(Stream.of("ID"))
                .build();
        final ManualRowsMappingTableBuilder target = new ManualRowsMappingTableBuilder(List.of(def), Source.NONE.sheetName("sheet"));
        Assertions.assertTrue(target.isTableStart(1));
        target.startNewTable();
        target.clearRowValue();
        target.handle(new CellReference(1, 1), -1, "id1");
        target.handle(new CellReference(1, 3), 1, "name1");
        Assertions.assertArrayEquals(new String[]{"name1", "id1"}, target.currentRow());
        Assertions.assertTrue(target.hasRow(1));
    }
}
