package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.Columns;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.operation.OperationData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class UpdateOperator extends DBOperator {

    public UpdateOperator(final IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        final Column[] columns = this.metaData.getColumns();
        final Column[] primaryKeys = this.metaData.getPrimaryKeys();
        if (primaryKeys.length == 0) {
            throw new NoPrimaryKeyException(this.metaData.getTableName());
        } else {
            final StringBuilder sqlBuffer = new StringBuilder(128);
            sqlBuffer.append("update ");
            sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
            final List<Column> columnList = new ArrayList<>(columns.length);
            sqlBuffer.append(" set ");
            IntStream.range(0, columns.length).forEach(i -> {
                final Column column = columns[i];
                if (Columns.getColumn(column.getColumnName(), primaryKeys) == null) {
                    if (columnList.size() > 0) {
                        sqlBuffer.append(", ");
                    }
                    final String columnName = this.getQualifiedName(null, column.getColumnName(), this.connection);
                    sqlBuffer.append(columnName);
                    sqlBuffer.append(" = ?");
                    columnList.add(column);
                }
            });
            sqlBuffer.append(" where ");
            IntStream.range(0, primaryKeys.length).forEach(i -> {
                final Column column = primaryKeys[i];
                if (i > 0) {
                    sqlBuffer.append(" and ");
                }
                final String columnName = this.getQualifiedName(null, column.getColumnName(), this.connection);
                sqlBuffer.append(columnName);
                sqlBuffer.append(" = ?");
                columnList.add(column);
            });
            return new OperationData(sqlBuffer.toString(), columnList.toArray(new Column[0]));
        }
    }
}
