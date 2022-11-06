package yo.dbunitcli.resource.poi;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.dbunit.dataset.stream.IDataSetConsumer;

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

    default XSSFSheetXMLHandler.SheetContentsHandler createHandler(final IDataSetConsumer consumer, final String sheetName, final boolean loadData) {
        return new XlsxSchemaHandler(consumer, sheetName, this, loadData);
    }

    default XlsxRowsToTableBuilder getRowsTableBuilder(final String sheetName) {
        return new FirstRowAsColumnTableBuilder(sheetName);
    }

    default XlsxCellsToTableBuilder getCellRecordBuilder(final String sheetName) {
        return XlsxCellsToTableBuilder.NO_TARGET;
    }

    class SimpleImpl implements XlsxSchema {

        private final Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap = new HashMap<>();

        private final Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap = new HashMap<>();

        protected SimpleImpl(final Builder builder) {
            this.rowsTableDefMap.putAll(builder.getRowsTableDefMap());
            this.cellsTableDefMap.putAll(builder.getCellsTableDefMap());
        }

        @Override
        public boolean contains(final String sheetName) {
            return this.rowsTableDefMap.containsKey(sheetName) || this.cellsTableDefMap.containsKey(sheetName);
        }

        @Override
        public XlsxRowsToTableBuilder getRowsTableBuilder(final String sheetName) {
            if (this.rowsTableDefMap.containsKey(sheetName)) {
                return new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(sheetName));
            }
            return ManualRowsMappingTableBuilder.NO_TARGET;
        }

        @Override
        public XlsxCellsToTableBuilder getCellRecordBuilder(final String sheetName) {
            if (this.cellsTableDefMap.containsKey(sheetName)) {
                return new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(sheetName));
            }
            return XlsxCellsToTableBuilder.NO_TARGET;
        }

        @Override
        public String toString() {
            return "SimpleImpl{" +
                    "rowsTableDefMap=" + this.rowsTableDefMap +
                    ", cellsTableDefMap=" + this.cellsTableDefMap +
                    '}';
        }
    }

    interface Builder {
        Map<String, List<XlsxRowsTableDefine>> getRowsTableDefMap();

        Map<String, List<XlsxCellsTableDefine>> getCellsTableDefMap();

        default XlsxSchema build() {
            return new SimpleImpl(this);
        }
    }
}
