package yo.dbunitcli.resource.poi;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record XlsxRowsTableDefine(String tableName
        , ITableMetaData tableMetaData
        , Integer dataStartRow
        , Integer[] cellIndexes
        , String[] breakKey
        , boolean addFileInfo) {

    public XlsxRowsTableDefine {
        // 読み込みを始めてから行ごとに気付くのでは遅いため、設定を読んだ時点で定義ミスにする
        final int headerCount = XlsxRowsTableDefine.columnCount(tableMetaData);
        if (cellIndexes.length > headerCount) {
            throw new IllegalArgumentException("xlsx-schema rows '" + tableName + "': columnIndex has "
                    + cellIndexes.length + " items but header has " + headerCount);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public XlsxRowsTableDefine(final Builder builder) {
        this(builder.getTableName()
                , builder.getTableMetaData()
                , builder.getDataStartRow()
                , builder.getCellIndexes().toArray(new Integer[0])
                , builder.getBreakKey().toArray(new String[0])
                , builder.getAddFileInfo());
    }

    private static int columnCount(final ITableMetaData tableMetaData) {
        try {
            return tableMetaData.getColumns().length;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public static class Builder {
        private final List<String> header = new ArrayList<>();
        private final List<Integer> cellIndexes = new ArrayList<>();
        private final List<String> breakKey = new ArrayList<>();
        private String tableName;
        private Integer dataStartRow;
        private boolean addFileInfo = false;

        public String getTableName() {
            return this.tableName;
        }

        public ITableMetaData getTableMetaData() {
            final Column[] columns = this.getHeader()
                    .stream()
                    .map(columnNames -> new Column(columnNames, DataType.UNKNOWN))
                    .toArray(Column[]::new);
            return new DefaultTableMetaData(this.tableName, columns);
        }

        public List<String> getHeader() {
            return this.header;
        }

        public Integer getDataStartRow() {
            return this.dataStartRow;
        }

        public List<Integer> getCellIndexes() {
            return this.cellIndexes;
        }

        public List<String> getBreakKey() {
            return this.breakKey;
        }

        public boolean getAddFileInfo() {
            return this.addFileInfo;
        }

        public Builder setTableName(final String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder setHeader(final Stream<String> header) {
            header.forEach(this.header::add);
            return this;
        }

        public Builder setDataStartRow(final Integer dataStartRow) {
            this.dataStartRow = dataStartRow;
            return this;
        }

        public Builder addCellIndexes(final Stream<Integer> cellIndexes) {
            cellIndexes.forEach(this.cellIndexes::add);
            return this;
        }

        public Builder addBreakKey(final Stream<String> breakKey) {
            breakKey.forEach(this.breakKey::add);
            return this;
        }

        public Builder setAddFileInfo(final boolean addFileInfo) {
            this.addFileInfo = addFileInfo;
            return this;
        }

        public XlsxRowsTableDefine build() {
            return new XlsxRowsTableDefine(this);
        }

    }
}
