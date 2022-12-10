package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.operation.OperationData;

import java.util.stream.IntStream;

public class DeleteOperator extends DBOperator {

    public DeleteOperator(final IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        final Column[] primaryKeys = this.metaData.getPrimaryKeys();
        if (primaryKeys.length == 0) {
            throw new NoPrimaryKeyException(this.metaData.getTableName());
        } else {
            final StringBuilder sqlBuffer = new StringBuilder(128);
            sqlBuffer.append("delete from ");
            sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
            sqlBuffer.append(" where ");
            IntStream.range(0, primaryKeys.length).forEach(i -> {
                final String columnName = this.getQualifiedName(null, primaryKeys[i].getColumnName(), this.connection);
                sqlBuffer.append(columnName);
                sqlBuffer.append(" = ?");
                if (i + 1 < primaryKeys.length) {
                    sqlBuffer.append(" and ");
                }
            });
            return new OperationData(sqlBuffer.toString(), primaryKeys);
        }
    }
}
