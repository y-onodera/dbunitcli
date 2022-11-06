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

public class XlsxCellsTableDefine {

    private final String tableName;
    private final int rowCount;
    private final int columnCount;
    private final ITableMetaData tableMetaData;
    private final Map<String, int[]> tableIndexMap = new HashMap<>();

    public XlsxCellsTableDefine(final Builder builder) {
        this.tableName = builder.getTableName();
        final List<String[]> rows = builder.getRows();
        this.rowCount = rows.size();
        final Column[] columns = builder.getHeader()
                .stream()
                .map(columnNames -> new Column(columnNames, DataType.UNKNOWN))
                .toArray(Column[]::new);
        this.columnCount = columns.length;
        this.tableMetaData = new DefaultTableMetaData(this.tableName, columns);
        IntStream.range(0, rows.size()).forEach(rowNum -> {
            final String[] row = rows.get(rowNum);
            IntStream.range(0, row.length).forEach(col -> this.tableIndexMap.put(row[col], new int[]{rowNum, col}));
        });
    }

    public static XlsxCellsTableDefine.Builder builder() {
        return new Builder();
    }

    public String getTableName() {
        return this.tableName;
    }

    public ITableMetaData getTableMetaData() {
        return this.tableMetaData;
    }

    public int columnCount() {
        return this.columnCount;
    }

    public int rowCount() {
        return this.rowCount;
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

    @Override
    public String toString() {
        return "XlsxCellsTableDefine{" +
                "tableName='" + this.tableName + '\'' +
                ", rowCount=" + this.rowCount +
                ", columnCount=" + this.columnCount +
                ", tableMetaData=" + this.tableMetaData +
                ", tableIndexMap=" + this.tableIndexMap +
                '}';
    }

    public static class Builder {
        private String tableName;
        private final List<String> header = new ArrayList<>();
        private final List<String[]> rows = new ArrayList<>();

        public String getTableName() {
            return this.tableName;
        }

        public List<String> getHeader() {
            return new ArrayList<>(this.header);
        }

        public List<String[]> getRows() {
            return new ArrayList<>(this.rows);
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

        public XlsxCellsTableDefine build() {
            return new XlsxCellsTableDefine(this);
        }
    }
}
