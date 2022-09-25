package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.operation.OperationData;

public class DeleteConsumer extends IDataSetOperationConsumer {

    public DeleteConsumer(IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        Column[] primaryKeys = this.metaData.getPrimaryKeys();
        if (primaryKeys.length == 0) {
            throw new NoPrimaryKeyException(this.metaData.getTableName());
        } else {
            StringBuilder sqlBuffer = new StringBuilder(128);
            sqlBuffer.append("delete from ");
            sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
            sqlBuffer.append(" where ");
            for (int i = 0; i < primaryKeys.length; ++i) {
                String columnName = this.getQualifiedName(null, primaryKeys[i].getColumnName(), this.connection);
                sqlBuffer.append(columnName);
                sqlBuffer.append(" = ?");
                if (i + 1 < primaryKeys.length) {
                    sqlBuffer.append(" and ");
                }
            }
            return new OperationData(sqlBuffer.toString(), primaryKeys);
        }
    }
}
