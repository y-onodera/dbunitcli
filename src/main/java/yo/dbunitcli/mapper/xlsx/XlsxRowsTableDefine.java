package yo.dbunitcli.mapper.xlsx;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.List;
import java.util.stream.Stream;

public class XlsxRowsTableDefine {
    private String tableName;
    private final int columnCount;
    private final ITableMetaData tableMetaData;
    private Integer dataStartRow;
    private Integer[] cellIndexes;
    private String[] breakKey;

    public XlsxRowsTableDefine(Builder builder) {
        this.tableName = builder.getTableName();
        String[] columnNames = builder.getHeader().toArray(new String[0]);
        this.columnCount = columnNames.length;
        final Column[] columns = new Column[columnNames.length];
        for (int i = 0, j = columns.length; i < j; i++) {
            columns[i] = new Column(columnNames[i], DataType.UNKNOWN);
        }
        this.tableMetaData = new DefaultTableMetaData(this.tableName, columns);
        this.dataStartRow = builder.getDataStartRow();
        this.cellIndexes = builder.getCellIndexes().toArray(new Integer[0]);
        this.breakKey = builder.getBreakKey().toArray(new String[0]);
    }

    public String getTableName() {
        return tableName;
    }

    public ITableMetaData getTableMetaData() {
        return this.tableMetaData;
    }

    public Integer getDataStartRow() {
        return dataStartRow;
    }

    public Integer[] getCellIndexes() {
        return cellIndexes;
    }

    public String[] getBreakKey() {
        return breakKey;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String tableName;
        private List<String> header = Lists.newArrayList();
        private Integer dataStartRow;
        private List<Integer> cellIndexes = Lists.newArrayList();
        private List<String> breakKey = Lists.newArrayList();

        public String getTableName() {
            return tableName;
        }

        public List<String> getHeader() {
            return header;
        }

        public Integer getDataStartRow() {
            return dataStartRow;
        }

        public List<Integer> getCellIndexes() {
            return cellIndexes;
        }

        public List<String> getBreakKey() {
            return breakKey;
        }

        public Builder setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder setHeader(Stream<String> header) {
            header.forEach(this.header::add);
            return this;
        }

        public Builder setDataStartRow(Integer dataStartRow) {
            this.dataStartRow = dataStartRow;
            return this;
        }

        public Builder addCellIndexes(Stream<Integer> cellIndexes) {
            cellIndexes.forEach(this.cellIndexes::add);
            return this;
        }

        public Builder addBreakKey(Stream<String> breakKey) {
            breakKey.forEach(this.breakKey::add);
            return this;
        }

        public XlsxRowsTableDefine build() {
            return new XlsxRowsTableDefine(this);
        }
    }
}
