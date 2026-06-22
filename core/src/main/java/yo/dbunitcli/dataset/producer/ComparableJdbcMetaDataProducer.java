package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.ComparableTableMappingTask;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ComparableJdbcMetaDataProducer extends ComparableDBDataSetProducer {

    private static final Column[] COLUMN_DEF_SCHEMA = {
            new Column("TABLE_NAME", DataType.VARCHAR),
            new Column("TABLE_REMARKS", DataType.VARCHAR),
            new Column("COLUMN_NAME", DataType.VARCHAR),
            new Column("TYPE_NAME", DataType.VARCHAR),
            new Column("COLUMN_SIZE", DataType.VARCHAR),
            new Column("DECIMAL_DIGITS", DataType.VARCHAR),
            new Column("NULLABLE", DataType.BOOLEAN),
            new Column("REMARKS", DataType.VARCHAR),
            new Column("IS_PK", DataType.BOOLEAN)
    };

    public ComparableJdbcMetaDataProducer(final ComparableDataSetParam param) {
        super(param);
    }

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new MetaDataTableExecutor(source, this.param, this.connection);
    }

    private static class MetaDataTableExecutor extends DBTableExecutor {

        MetaDataTableExecutor(final Source source, final ComparableDataSetParam param,
                              final org.dbunit.database.IDatabaseConnection connection) {
            super(source, param, connection);
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            final String tableName = this.source.tableName();
            try {
                final DatabaseMetaData meta = this.connection.getConnection().getMetaData();
                final Set<String> pkColumns = loadPrimaryKeys(meta, tableName);
                final String tableRemarks = loadTableRemarks(meta, tableName);
                final var metaData = new DefaultTableMetaData(tableName, COLUMN_DEF_SCHEMA);
                final var mapper = context.createMapper(this.source.wrap(metaData));
                mapper.startTable();
                try (ResultSet colRs = meta.getColumns(null, null, tableName, "%")) {
                    while (colRs.next()) {
                        final String colName = colRs.getString("COLUMN_NAME");
                        final String typeName = colRs.getString("TYPE_NAME");
                        final String colSize = colRs.getString("COLUMN_SIZE");
                        final String decDigits = colRs.getString("DECIMAL_DIGITS");
                        final boolean nullable = colRs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                        final String remarks = colRs.getString("REMARKS");
                        mapper.addRow(new Object[]{
                                tableName, tableRemarks,
                                colName, typeName, colSize, decDigits, nullable,
                                remarks != null ? remarks : "",
                                pkColumns.contains(colName)
                        });
                    }
                }
                mapper.endTable();
            } catch (final SQLException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new MetaDataTableExecutor(this.source, builder.build(), this.connection);
        }

        private static Set<String> loadPrimaryKeys(final DatabaseMetaData meta, final String tableName)
                throws SQLException {
            final Set<String> pkColumns = new HashSet<>();
            try (ResultSet pkRs = meta.getPrimaryKeys(null, null, tableName)) {
                while (pkRs.next()) {
                    pkColumns.add(pkRs.getString("COLUMN_NAME"));
                }
            }
            return pkColumns;
        }

        private static String loadTableRemarks(final DatabaseMetaData meta, final String tableName)
                throws SQLException {
            try (ResultSet tableRs = meta.getTables(null, null, tableName, null)) {
                if (tableRs.next()) {
                    final String remarks = tableRs.getString("REMARKS");
                    return remarks != null ? remarks : "";
                }
            }
            return "";
        }
    }
}
