package yo.dbunitcli.resource.poi;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class XlsxRowsTableDefine {
    private final String tableName;
    private final ITableMetaData tableMetaData;
    private final Integer dataStartRow;
    private final Integer[] cellIndexes;
    private final String[] breakKey;

    public XlsxRowsTableDefine(final Builder builder) {
        this.tableName = builder.getTableName();
        final Column[] columns = builder.getHeader()
                .stream()
                .map(columnNames -> new Column(columnNames, DataType.UNKNOWN))
                .toArray(Column[]::new);
        this.tableMetaData = new DefaultTableMetaData(this.tableName, columns);
        this.dataStartRow = builder.getDataStartRow();
        this.cellIndexes = builder.getCellIndexes().toArray(new Integer[0]);
        this.breakKey = builder.getBreakKey().toArray(new String[0]);
    }

    public String getTableName() {
        return this.tableName;
    }

    public ITableMetaData getTableMetaData() {
        return this.tableMetaData;
    }

    public Integer getDataStartRow() {
        return this.dataStartRow;
    }

    public Integer[] getCellIndexes() {
        return this.cellIndexes;
    }

    public String[] getBreakKey() {
        return this.breakKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "XlsxRowsTableDefine{" +
                "tableName='" + this.tableName + '\'' +
                ", tableMetaData=" + this.tableMetaData +
                ", dataStartRow=" + this.dataStartRow +
                ", cellIndexes=" + Arrays.toString(this.cellIndexes) +
                ", breakKey=" + Arrays.toString(this.breakKey) +
                '}';
    }

    public static class Builder {
        private String tableName;
        private final List<String> header = new ArrayList<>();
        private Integer dataStartRow;
        private final List<Integer> cellIndexes = new ArrayList<>();
        private final List<String> breakKey = new ArrayList<>();

        public String getTableName() {
            return this.tableName;
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

        public XlsxRowsTableDefine build() {
            return new XlsxRowsTableDefine(this);
        }
    }
}
