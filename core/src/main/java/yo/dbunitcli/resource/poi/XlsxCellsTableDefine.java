package yo.dbunitcli.resource.poi;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record XlsxCellsTableDefine(String tableName
        , int rowCount
        , int columnCount
        , ITableMetaData tableMetaData
        , Map<String, int[]> tableIndexMap
        , boolean addOptional
) {

    public static XlsxCellsTableDefine.Builder builder() {
        return new Builder();
    }

    public XlsxCellsTableDefine(final Builder builder) {
        this(builder.getTableName()
                , builder.getRowCount()
                , builder.getNumberOfColumns()
                , builder.getTableMetaData()
                , builder.getTableIndexMap()
                , builder.getAddFileInfo());
    }

    public String getColumnName(final String ref) throws DataSetException {
        return this.tableMetaData.getColumns()[this.tableIndexMap.get(ref)[1]].getColumnName();
    }

    public int getRowIndex(final String ref) {
        return this.tableIndexMap.get(ref)[0];
    }

    public Iterable<String> getTargetAddresses() {
        return this.tableIndexMap.keySet();
    }

    public static class Builder {
        private String tableName;
        private final List<String> header = new ArrayList<>();
        private final List<String[]> rows = new ArrayList<>();
        private boolean addFileInfo = false;

        public String getTableName() {
            return this.tableName;
        }

        public ITableMetaData getTableMetaData() {
            return new DefaultTableMetaData(this.tableName, this.getColumns());
        }

        public int getNumberOfColumns() {
            return this.getColumns().length;
        }

        public List<String> getHeader() {
            return new ArrayList<>(this.header);
        }

        public int getRowCount() {
            return this.rows.size();
        }

        public Map<String, int[]> getTableIndexMap() {
            final var result = new HashMap<String, int[]>();
            IntStream.range(0, this.rows.size()).forEach(rowNum -> {
                final String[] row = this.rows.get(rowNum);
                IntStream.range(0, row.length).forEach(col -> result.put(row[col], new int[]{rowNum, col}));
            });
            return result;
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

        public Builder setRows(final Stream<Stream<String>> rows) {
            rows.forEach(it -> this.rows.add(it.toArray(String[]::new)));
            return this;
        }

        public Builder setAddFileInfo(final boolean addFileInfo) {
            this.addFileInfo = addFileInfo;
            return this;
        }

        public XlsxCellsTableDefine build() {
            return new XlsxCellsTableDefine(this);
        }

        private Column[] getColumns() {
            return this.getHeader()
                    .stream()
                    .map(columnNames -> new Column(columnNames, DataType.UNKNOWN))
                    .toArray(Column[]::new);
        }
    }
}
