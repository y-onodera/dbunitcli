package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class JsonXlsxSchemaDto {

    private List<RowSetting> rows;
    private List<CellSetting> cells;

    public List<RowSetting> getRows() {
        return this.rows;
    }

    public void setRows(final List<RowSetting> rows) {
        this.rows = rows;
    }

    public List<CellSetting> getCells() {
        return this.cells;
    }

    public void setCells(final List<CellSetting> cells) {
        this.cells = cells;
    }

    @Serdeable
    public static class RowSetting {
        private String sheetName;
        private String tableName;
        private List<String> header;
        private int dataStart;
        private List<Integer> columnIndex;
        private List<String> breakKey;
        private boolean addFileInfo;

        // Getters and Setters
        public String getSheetName() {
            return this.sheetName;
        }

        public void setSheetName(final String sheetName) {
            this.sheetName = sheetName;
        }

        public String getTableName() {
            return this.tableName;
        }

        public void setTableName(final String tableName) {
            this.tableName = tableName;
        }

        public List<String> getHeader() {
            return this.header;
        }

        public void setHeader(final List<String> header) {
            this.header = header;
        }

        public int getDataStart() {
            return this.dataStart;
        }

        public void setDataStart(final int dataStart) {
            this.dataStart = dataStart;
        }

        public List<Integer> getColumnIndex() {
            return this.columnIndex;
        }

        public void setColumnIndex(final List<Integer> columnIndex) {
            this.columnIndex = columnIndex;
        }

        public List<String> getBreakKey() {
            return this.breakKey;
        }

        public void setBreakKey(final List<String> breakKey) {
            this.breakKey = breakKey;
        }

        public boolean isAddFileInfo() {
            return this.addFileInfo;
        }

        public void setAddFileInfo(final boolean addFileInfo) {
            this.addFileInfo = addFileInfo;
        }
    }

    @Serdeable
    public static class CellSetting {
        private String sheetName;
        private String tableName;
        private List<String> header;
        private List<Row> rows;
        private boolean addFileInfo;

        // Getters and Setters
        public String getSheetName() {
            return this.sheetName;
        }

        public void setSheetName(final String sheetName) {
            this.sheetName = sheetName;
        }

        public String getTableName() {
            return this.tableName;
        }

        public void setTableName(final String tableName) {
            this.tableName = tableName;
        }

        public List<String> getHeader() {
            return this.header;
        }

        public void setHeader(final List<String> header) {
            this.header = header;
        }

        public List<Row> getRows() {
            return this.rows;
        }

        public void setRows(final List<Row> rows) {
            this.rows = rows;
        }

        public boolean isAddFileInfo() {
            return this.addFileInfo;
        }

        public void setAddFileInfo(final boolean addFileInfo) {
            this.addFileInfo = addFileInfo;
        }

        @Serdeable
        public static class Row {
            private List<String> cellAddress;

            // Getters and Setters
            public List<String> getCellAddress() {
                return this.cellAddress;
            }

            public void setCellAddress(final List<String> cellAddress) {
                this.cellAddress = cellAddress;
            }
        }
    }
}