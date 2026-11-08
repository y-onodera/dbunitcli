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
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record XlsxCellsTableDefine(String tableName
        , int rowCount
        , int columnCount
        , ITableMetaData tableMetaData
        , Map<String, List<int[]>> tableIndexMap
        , boolean addFileInfo
) {

    /**
     * 読み込み中のセルの番地（シート名・$なし）と同じ形。それ以外の書き方はどのセルとも一致しない
     */
    private static final Pattern CELL_ADDRESS = Pattern.compile("[A-Z]{1,3}[1-9][0-9]*");

    public XlsxCellsTableDefine {
        // 読み込みを始めてから気付くのでは遅いため、設定を読んだ時点で定義ミスにする
        for (final Map.Entry<String, List<int[]>> entry : tableIndexMap.entrySet()) {
            if (!CELL_ADDRESS.matcher(entry.getKey()).matches()) {
                throw new IllegalArgumentException("xlsx-schema cells '" + tableName + "': cellAddress '"
                        + entry.getKey() + "' is not A1 format");
            }
            for (final int[] position : entry.getValue()) {
                if (position[1] >= columnCount) {
                    throw new IllegalArgumentException("xlsx-schema cells '" + tableName + "': rows[" + position[0]
                            + "].cellAddress has more items than header " + columnCount);
                }
            }
        }
    }

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

    /**
     * @return refを割り当てた位置（{行, 列}）。1つのセルを複数のカラムに割り当てられる
     */
    public List<int[]> getPositions(final String ref) {
        return this.tableIndexMap.getOrDefault(ref, List.of());
    }

    public String getColumnName(final int column) throws DataSetException {
        return this.tableMetaData.getColumns()[column].getColumnName();
    }

    public Iterable<String> getTargetAddresses() {
        return this.tableIndexMap.keySet();
    }

    public static class Builder {
        private final List<String> header = new ArrayList<>();
        private final List<String[]> rows = new ArrayList<>();
        private String tableName;
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

        public Map<String, List<int[]>> getTableIndexMap() {
            final var result = new HashMap<String, List<int[]>>();
            IntStream.range(0, this.rows.size()).forEach(rowNum -> {
                final String[] row = this.rows.get(rowNum);
                IntStream.range(0, row.length)
                        .forEach(col -> result.computeIfAbsent(row[col], k -> new ArrayList<>()).add(new int[]{rowNum, col}));
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
