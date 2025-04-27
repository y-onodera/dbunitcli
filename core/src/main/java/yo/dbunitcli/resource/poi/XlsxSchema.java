package yo.dbunitcli.resource.poi;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.common.filter.TargetFilter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    default XSSFSheetXMLHandler.SheetContentsHandler createHandler(final IDataSetConsumer consumer, final String sheetName, final int startRow, final String[] headerNames, final boolean loadData) {
        return new XlsxSchemaHandler(consumer, sheetName, startRow, headerNames, this, loadData);
    }

    default XlsxRowsToTableBuilder getRowsTableBuilder(final String sheetName, final int startRow, final String[] headerNames) {
        return new StartRowAsColumnTableBuilder(sheetName, startRow, headerNames);
    }

    default XlsxCellsToTableBuilder getCellRecordBuilder(final String sheetName, final String[] headerNames) {
        return XlsxCellsToTableBuilder.NO_TARGET;
    }

    default XlsxSchema addFileInfo(final File sourceFile, final String sheetName) {
        return this;
    }

    interface Builder {
        Map<String, List<XlsxRowsTableDefine>> getRowsTableDefMap();

        Map<String, List<XlsxCellsTableDefine>> getCellsTableDefMap();

        Map<String, TargetFilter> getSheetPatterns();

        default XlsxSchema build() {
            return new SimpleImpl(this);
        }
    }

    record FileInfo(String filePath, String fileName, String sheetName) {
        public static FileInfo NONE = new FileInfo("", "", "");

        public ITableMetaData wrap(final ITableMetaData tableMetaData) {
            if (this == NONE) {
                return tableMetaData;
            }
            try {
                return new AddFileInfoMetaData(tableMetaData, this);
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        }

        public void setValueTo(final List<String> rowValues) {
            rowValues.set(rowValues.size() - 3, this.filePath());
            rowValues.set(rowValues.size() - 2, this.fileName());
            rowValues.set(rowValues.size() - 1, this.sheetName());
        }

        public String[] defaultColumnValues(final int columnCount) {
            final String[] result = new String[columnCount + 3];
            result[columnCount] = this.filePath;
            result[columnCount + 1] = this.fileName;
            result[columnCount + 2] = this.sheetName;
            return result;
        }

        public static class AddFileInfoMetaData extends DefaultTableMetaData {

            public static Column[] OPTION_COLUMNS = new Column[]{
                    new Column("$FILE_PATH", DataType.NVARCHAR)
                    , new Column("$FILE_NAME", DataType.NVARCHAR)
                    , new Column("$SHEET_NAME", DataType.NVARCHAR)
            };

            private final FileInfo fileInfo;

            private static Column[] getColumns(final ITableMetaData tableMetaData) throws DataSetException {
                return Stream.concat(Arrays.stream(tableMetaData.getColumns()), Arrays.stream(OPTION_COLUMNS))
                        .toArray(Column[]::new);
            }

            private AddFileInfoMetaData(final ITableMetaData tableMetaData, final FileInfo fileInfo) throws DataSetException {
                super(tableMetaData.getTableName(), getColumns(tableMetaData), tableMetaData.getPrimaryKeys());
                this.fileInfo = fileInfo;
            }

            public void setValueTo(final List<String> rowValues) {
                this.fileInfo.setValueTo(rowValues);
            }
        }

    }

    record SimpleImpl(Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap
            , Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap
            , Map<String, TargetFilter> sheetPatterns
            , FileInfo fileInfo) implements XlsxSchema {

        SimpleImpl(final Builder builder) {
            this(new HashMap<>(builder.getRowsTableDefMap()),
                    new HashMap<>(builder.getCellsTableDefMap()),
                    new HashMap<>(builder.getSheetPatterns()),
                    FileInfo.NONE);
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
        public XlsxRowsToTableBuilder getRowsTableBuilder(final String sheetName, final int startRow, final String[] headerNames) {
            if (this.rowsTableDefMap.containsKey(sheetName)) {
                return new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(sheetName), this.fileInfo());
            }
            final var matchingPattern = this.sheetPatterns.entrySet().stream()
                    .filter(entry -> entry.getValue().test(sheetName))
                    .map(Map.Entry::getKey)
                    .filter(this.rowsTableDefMap::containsKey)
                    .findFirst();
            return matchingPattern
                    .map(s -> new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(s), this.fileInfo()))
                    .orElseGet(() -> ManualRowsMappingTableBuilder.NO_TARGET);
        }

        @Override
        public XlsxCellsToTableBuilder getCellRecordBuilder(final String sheetName, final String[] headerNames) {
            if (this.cellsTableDefMap.containsKey(sheetName)) {
                return new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(sheetName), this.fileInfo());
            }
            final var matchingPattern = this.sheetPatterns.entrySet().stream()
                    .filter(entry -> entry.getValue().test(sheetName))
                    .map(Map.Entry::getKey)
                    .filter(this.cellsTableDefMap::containsKey)
                    .findFirst();
            return matchingPattern
                    .map(s -> new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(s), this.fileInfo()))
                    .orElseGet(() -> XlsxCellsToTableBuilder.NO_TARGET);
        }

        @Override
        public XlsxSchema addFileInfo(final File sourceFile, final String sheetName) {
            return new SimpleImpl(this.rowsTableDefMap(), this.cellsTableDefMap(), this.sheetPatterns()
                    , new FileInfo(sourceFile.getAbsolutePath().replaceAll("\\\\", "/"), sourceFile.getName(), sheetName));
        }
    }
}
