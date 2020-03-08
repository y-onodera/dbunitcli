package yo.dbunitcli.mapper.xlsx;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public interface XlsxSchema {

    XlsxSchema DEFAULT = new XlsxSchema() {
    };

    default boolean contains(String sheetName) {
        return true;
    }

    default XlsxRowsToTableBuilder getRowsTableBuilder(String sheetName) {
        return new FirstRowAsColumnTableBuilder(sheetName);
    }

    default XlsxCellsToTableBuilder getCellRecordBuilder(String sheetName) {
        return XlsxCellsToTableBuilder.NO_TARGET;
    }

    class SimpleImpl implements XlsxSchema {

        private Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap = Maps.newHashMap();

        private Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap = Maps.newHashMap();

        protected SimpleImpl(Builder builder) {
            this.rowsTableDefMap.putAll(builder.getRowsTableDefMap());
            this.cellsTableDefMap.putAll(builder.getCellsTableDefMap());
        }

        @Override
        public boolean contains(String sheetName) {
            return this.rowsTableDefMap.containsKey(sheetName) || this.cellsTableDefMap.containsKey(sheetName);
        }

        @Override
        public XlsxRowsToTableBuilder getRowsTableBuilder(String sheetName) {
            if (this.rowsTableDefMap.containsKey(sheetName)) {
                return new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(sheetName));
            }
            return ManualRowsMappingTableBuilder.NO_TARGET;
        }

        @Override
        public XlsxCellsToTableBuilder getCellRecordBuilder(String sheetName) {
            if (this.cellsTableDefMap.containsKey(sheetName)) {
                return new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(sheetName));
            }
            return XlsxCellsToTableBuilder.NO_TARGET;
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
