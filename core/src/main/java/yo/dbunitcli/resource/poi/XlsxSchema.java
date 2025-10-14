package yo.dbunitcli.resource.poi;

import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TargetFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface XlsxSchema {

    XlsxSchema DEFAULT = new XlsxSchema() {
        @Override
        public String toString() {
            return "XlsxSchema{}";
        }
    };

    default boolean contains(final String sheetName) {
        return true;
    }

    default XlsxRowsToTableBuilder getRowsTableBuilder(final int startRow, final String[] headerNames, final Source source) {
        return new StartRowAsColumnTableBuilder(startRow, headerNames, source);
    }

    default XlsxCellsToTableBuilder getCellRecordBuilder(final String[] headerNames, final Source source) {
        return XlsxCellsToTableBuilder.NO_TARGET;
    }

    interface Builder {
        Map<String, List<XlsxRowsTableDefine>> getRowsTableDefMap();

        Map<String, List<XlsxCellsTableDefine>> getCellsTableDefMap();

        Map<String, TargetFilter> getSheetPatterns();

        default XlsxSchema build() {
            return new SimpleImpl(this);
        }
    }

    record SimpleImpl(Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap
            , Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap
            , Map<String, TargetFilter> sheetPatterns
            , Source source) implements XlsxSchema {

        SimpleImpl(final Builder builder) {
            this(new HashMap<>(builder.getRowsTableDefMap()),
                    new HashMap<>(builder.getCellsTableDefMap()),
                    new HashMap<>(builder.getSheetPatterns()),
                    Source.NONE);
        }

        @Override
        public boolean contains(final String sheetName) {
            if (this.rowsTableDefMap.containsKey(sheetName) || this.cellsTableDefMap.containsKey(sheetName)) {
                return true;
            }
            return this.sheetPatterns.entrySet().stream()
                    .anyMatch(entry -> entry.getValue().test(sheetName));
        }

        @Override
        public XlsxRowsToTableBuilder getRowsTableBuilder(final int startRow
                , final String[] headerNames
                , final Source source) {
            if (this.rowsTableDefMap.containsKey(source.sheetName())) {
                return new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(source.sheetName()), source);
            }
            final var matchingPattern = this.sheetPatterns.entrySet().stream()
                    .filter(entry -> entry.getValue().test(source.sheetName()))
                    .map(Map.Entry::getKey)
                    .filter(this.rowsTableDefMap::containsKey)
                    .findFirst();
            return matchingPattern
                    .map(s -> new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(s), source))
                    .orElseGet(() -> ManualRowsMappingTableBuilder.NO_TARGET);
        }

        @Override
        public XlsxCellsToTableBuilder getCellRecordBuilder(final String[] headerNames, final Source source) {
            if (this.cellsTableDefMap.containsKey(source.sheetName())) {
                return new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(source.sheetName()), source);
            }
            final var matchingPattern = this.sheetPatterns.entrySet().stream()
                    .filter(entry -> entry.getValue().test(source.sheetName()))
                    .map(Map.Entry::getKey)
                    .filter(this.cellsTableDefMap::containsKey)
                    .findFirst();
            return matchingPattern
                    .map(s -> new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(s), source))
                    .orElseGet(() -> XlsxCellsToTableBuilder.NO_TARGET);
        }
    }
}
