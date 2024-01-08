package yo.dbunitcli.resource.poi;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.IDataSetConsumer;

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

    default XSSFSheetXMLHandler.SheetContentsHandler createHandler(final IDataSetConsumer consumer, final String sheetName, final boolean loadData) {
        return new XlsxSchemaHandler(consumer, sheetName, this, loadData);
    }

    default XlsxRowsToTableBuilder getRowsTableBuilder(final String sheetName) {
        return new FirstRowAsColumnTableBuilder(sheetName);
    }

    default XlsxCellsToTableBuilder getCellRecordBuilder(final String sheetName) {
        return XlsxCellsToTableBuilder.NO_TARGET;
    }

    default XlsxSchema addFileInfo(final File sourceFile, final String sheetName) {
        return this;
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

            private AddFileInfoMetaData(final ITableMetaData tableMetaData, final FileInfo fileInfo) throws DataSetException {
                super(tableMetaData.getTableName(), getColumns(tableMetaData), tableMetaData.getPrimaryKeys());
                this.fileInfo = fileInfo;
            }

            private static Column[] getColumns(final ITableMetaData tableMetaData) throws DataSetException {
                return Stream.concat(Arrays.stream(tableMetaData.getColumns()), Arrays.stream(OPTION_COLUMNS))
                        .toArray(Column[]::new);
            }

            public void setValueTo(final List<String> rowValues) {
                this.fileInfo.setValueTo(rowValues);
            }
        }

    }

    record SimpleImpl(Map<String, List<XlsxRowsTableDefine>> rowsTableDefMap
            , Map<String, List<XlsxCellsTableDefine>> cellsTableDefMap
            , FileInfo fileInfo) implements XlsxSchema {

        SimpleImpl(final Builder builder) {
            this(new HashMap<>(builder.getRowsTableDefMap()), new HashMap<>(builder.getCellsTableDefMap()), FileInfo.NONE);
        }

        @Override
        public boolean contains(final String sheetName) {
            return this.rowsTableDefMap.containsKey(sheetName) || this.cellsTableDefMap.containsKey(sheetName);
        }

        @Override
        public XlsxRowsToTableBuilder getRowsTableBuilder(final String sheetName) {
            if (this.rowsTableDefMap.containsKey(sheetName)) {
                return new ManualRowsMappingTableBuilder(this.rowsTableDefMap.get(sheetName), this.fileInfo());
            }
            return ManualRowsMappingTableBuilder.NO_TARGET;
        }

        @Override
        public XlsxCellsToTableBuilder getCellRecordBuilder(final String sheetName) {
            if (this.cellsTableDefMap.containsKey(sheetName)) {
                return new XlsxCellsToTableBuilder(this.cellsTableDefMap.get(sheetName), this.fileInfo());
            }
            return XlsxCellsToTableBuilder.NO_TARGET;
        }

        @Override
        public XlsxSchema addFileInfo(final File sourceFile, final String sheetName) {
            return new SimpleImpl(this.rowsTableDefMap(), this.cellsTableDefMap()
                    , new FileInfo(sourceFile.getAbsolutePath().replaceAll("\\\\", "/"), sourceFile.getName(), sheetName));
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
