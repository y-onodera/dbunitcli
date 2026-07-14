package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableDataSetProducerWrapper;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ComparableJdbcMetaDataProducer extends ComparableDataSetProducerWrapper {

    // 列集合・順序はComparableDdlMetaDataProducer.outputSchema()を唯一の定義として導出する
    // （ScaffoldTarget.datasetSchema()の-headerNameやjavaBeanSettings.jsonのPACKAGE式と整合させるため）。
    // JDBCから実値を取れるNULLABLE/IS_PKのみBOOLEAN型に差し替える
    private static final Column[] COLUMN_DEF_SCHEMA = Arrays.stream(ComparableDdlMetaDataProducer.outputSchema())
            .map(column -> Set.of("NULLABLE", "IS_PK").contains(column.getColumnName())
                    ? new Column(column.getColumnName(), DataType.BOOLEAN)
                    : column)
            .toArray(Column[]::new);

    private final DatabaseMetaData jdbcMetaData;

    public ComparableJdbcMetaDataProducer(final ComparableDataSetProducer delegate) {
        super(delegate);
        this.jdbcMetaData = resolveJdbcMetaData(delegate);
    }

    private static DatabaseMetaData resolveJdbcMetaData(final ComparableDataSetProducer delegate) {
        try {
            if (delegate instanceof final ComparableDBDataSetProducer dbProducer) {
                return dbProducer.connection.getConnection().getMetaData();
            }
            final var connectionLoader = delegate.param().databaseConnectionLoader();
            if (connectionLoader == null) {
                return null;
            }
            return connectionLoader.loadConnection().getConnection().getMetaData();
        } catch (final SQLException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        try {
            final String tableName = metaData.getTableName();
            final String tableRemarks = this.jdbcMetaData != null ? loadTableRemarks(this.jdbcMetaData, tableName) : "";
            final Map<String, String> pkNames = this.jdbcMetaData != null
                    ? loadPrimaryKeyNames(this.jdbcMetaData, tableName)
                    : Arrays.stream(metaData.getPrimaryKeys())
                            .collect(Collectors.toMap(Column::getColumnName, column -> (String) null, (a, b) -> a, LinkedHashMap::new));
            final Map<String, ColumnDetail> columnDetails = this.jdbcMetaData != null ? loadColumnDetails(this.jdbcMetaData, tableName) : Map.of();
            this.writeColumnRows(tableName, COLUMN_DEF_SCHEMA, metaData.getColumns(), (column, index) -> {
                final String columnName = column.getColumnName();
                final ColumnDetail detail = columnDetails.get(columnName);
                return new Object[]{
                        columnName,
                        detail != null ? detail.typeName() : column.getDataType().toString(),
                        detail != null ? detail.columnSize() : null,
                        detail != null ? detail.decimalDigits() : null,
                        !column.isNotNullable(),
                        pkNames.containsKey(columnName),
                        pkNames.get(columnName),
                        detail != null ? detail.remarks() : "",
                        tableRemarks,
                        tableName,
                        ""
                };
            });
        } catch (final SQLException e) {
            throw new AssertionError(e);
        }
    }

    private static Map<String, String> loadPrimaryKeyNames(final DatabaseMetaData meta, final String tableName)
            throws SQLException {
        return collectByColumnName(meta.getPrimaryKeys(null, null, tableName), rs -> rs.getString("PK_NAME"));
    }

    private static String loadTableRemarks(final DatabaseMetaData meta, final String tableName)
            throws SQLException {
        try (ResultSet tableRs = meta.getTables(null, null, tableName, null)) {
            if (tableRs.next()) {
                return Optional.ofNullable(tableRs.getString("REMARKS")).orElse("");
            }
        }
        return "";
    }

    private static Map<String, ColumnDetail> loadColumnDetails(final DatabaseMetaData meta, final String tableName)
            throws SQLException {
        return collectByColumnName(meta.getColumns(null, null, tableName, "%"), rs -> {
            final String typeName = rs.getString("TYPE_NAME");
            final String columnSize = resolveColumnSize(typeName, rs.getInt("DATA_TYPE"), rs.getString("COLUMN_SIZE"));
            final String decimalDigits = resolveDecimalDigits(rs.getString("DECIMAL_DIGITS"));
            final String remarks = rs.getString("REMARKS");
            return new ColumnDetail(typeName, columnSize, decimalDigits, remarks != null ? remarks : "");
        });
    }

    private static <T> Map<String, T> collectByColumnName(final ResultSet rs, final SqlValueMapper<T> valueMapper)
            throws SQLException {
        try (rs) {
            final Map<String, T> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("COLUMN_NAME"), valueMapper.map(rs));
            }
            return result;
        }
    }

    private static String resolveColumnSize(final String typeName, final int jdbcType, final String columnSize) {
        if (typeName.contains("(")) {
            return null;
        }
        return switch (jdbcType) {
            case Types.DATE, Types.TIME, Types.TIMESTAMP,
                 Types.BLOB, Types.CLOB, Types.NCLOB,
                 Types.BOOLEAN, Types.BIT -> null;
            default -> columnSize;
        };
    }

    private static String resolveDecimalDigits(final String decimalDigits) {
        return "0".equals(decimalDigits) || decimalDigits == null ? null : decimalDigits;
    }

    @FunctionalInterface
    private interface SqlValueMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private record ColumnDetail(String typeName, String columnSize, String decimalDigits, String remarks) {
    }
}
