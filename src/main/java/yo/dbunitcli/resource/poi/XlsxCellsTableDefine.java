package yo.dbunitcli.resource.poi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class XlsxCellsTableDefine {

    private final String tableName;
    private final int rowCount;
    private final int columnCount;
    private final ITableMetaData tableMetaData;
    private Map<String, int[]> tableIndexMap = Maps.newHashMap();

    public XlsxCellsTableDefine(Builder builder) {
        this.tableName = builder.getTableName();
        List<String[]> rows = builder.getRows();
        this.rowCount = rows.size();
        String[] columnNames = builder.getHeader().toArray(new String[0]);
        this.columnCount = columnNames.length;
        final Column[] columns = new Column[columnNames.length];
        for (int i = 0, j = columns.length; i < j; i++) {
            columns[i] = new Column(columnNames[i], DataType.UNKNOWN);
        }
        this.tableMetaData = new DefaultTableMetaData(this.tableName, columns);
        for (int rowNum = 0, maxRow = rows.size(); rowNum < maxRow; rowNum++) {
            String[] row = rows.get(rowNum);
            for (int col = 0, maxCol = row.length; col < maxCol; col++) {
                this.tableIndexMap.put(row[col], new int[]{rowNum, col});
            }
        }
    }

    public static XlsxCellsTableDefine.Builder builder() {
        return new Builder();
    }

    public String getTableName() {
        return tableName;
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

    public String getColumnName(String ref) throws DataSetException {
        return this.tableMetaData.getColumns()[this.tableIndexMap.get(ref)[1]].getColumnName();
    }

    public int getRowIndex(String ref) {
        return this.tableIndexMap.get(ref)[0];
    }

    public Iterable<String> getTargetAddresses() {
        return this.tableIndexMap.keySet();
    }

    public static class Builder {
        private String tableName;
        private List<String> header = Lists.newArrayList();
        private List<String[]> rows = Lists.newArrayList();

        public String getTableName() {
            return tableName;
        }

        public List<String> getHeader() {
            return header;
        }

        public List<String[]> getRows() {
            return rows;
        }

        public Builder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder setHeader(Stream<String> header) {
            header.forEach(this.header::add);
            return this;
        }

        public Builder setRows(Stream<Stream<String>> rows) {
            rows.forEach(it -> this.rows.add(it.toArray(String[]::new)));
            return this;
        }

        public XlsxCellsTableDefine build() {
            return new XlsxCellsTableDefine(this);
        }
    }
}
