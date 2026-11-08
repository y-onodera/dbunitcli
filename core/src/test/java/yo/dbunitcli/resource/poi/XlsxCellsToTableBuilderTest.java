package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TargetFilter;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class XlsxCellsToTableBuilderTest {

    private static XlsxCellsTableDefine.Builder define(final String tableName, final Stream<String> header, final Stream<Stream<String>> rows) {
        return XlsxCellsTableDefine.builder()
                .setTableName(tableName)
                .setHeader(header)
                .setRows(rows);
    }

    @Test
    void oneCellCanBeMappedToMultipleColumns() {
        final XlsxCellsTableDefine def = define("TABLE", Stream.of("A", "B", "C"), Stream.of(Stream.of("A1", "B1", "A1"))).build();
        final XlsxCellsToTableBuilder target = new XlsxCellsToTableBuilder(List.of(def), Source.NONE.sheetName("sheet"));
        target.handle(new CellReference(0, 0), "a");
        target.handle(new CellReference(0, 1), "b");
        Assertions.assertArrayEquals(new String[]{"a", "b", "a"}, target.getRows("TABLE").getFirst());
    }

    @Test
    void cellAddressMoreThanHeaderIsDefinitionError() {
        final XlsxCellsTableDefine.Builder builder = define("TABLE", Stream.of("A"), Stream.of(Stream.of("A1"), Stream.of("A2", "B2")));
        final IllegalArgumentException actual = Assertions.assertThrows(IllegalArgumentException.class, builder::build);
        Assertions.assertEquals("xlsx-schema cells 'TABLE': rows[1].cellAddress has more items than header 1", actual.getMessage());
    }

    @Test
    void cellAddressNotA1FormatIsDefinitionError() {
        for (final String address : List.of("a1", "$A$1", "Sheet1!A1", " A1", "", "A0", "abc")) {
            final XlsxCellsTableDefine.Builder builder = define("TABLE", Stream.of("A"), Stream.of(Stream.of(address)));
            final IllegalArgumentException actual = Assertions.assertThrows(IllegalArgumentException.class, builder::build, address);
            Assertions.assertEquals("xlsx-schema cells 'TABLE': cellAddress '" + address + "' is not A1 format", actual.getMessage());
        }
    }

    @Test
    void sameTableNameInSameSheetIsDefinitionError() {
        final XlsxCellsTableDefine first = define("TABLE", Stream.of("A"), Stream.of(Stream.of("A1"))).build();
        final XlsxCellsTableDefine second = define("TABLE", Stream.of("B"), Stream.of(Stream.of("B1"))).build();
        final XlsxSchema.Builder builder = new XlsxSchema.Builder() {
            @Override
            public Map<String, List<XlsxRowsTableDefine>> getRowsTableDefMap() {
                return Map.of();
            }

            @Override
            public Map<String, List<XlsxCellsTableDefine>> getCellsTableDefMap() {
                return Map.of("sheet", List.of(first, second));
            }

            @Override
            public Map<String, TargetFilter> getSheetPatterns() {
                return Map.of();
            }
        };
        final IllegalArgumentException actual = Assertions.assertThrows(IllegalArgumentException.class, builder::build);
        Assertions.assertEquals("xlsx-schema cells 'sheet': tableName 'TABLE' is defined more than once", actual.getMessage());
    }
}
