package yo.dbunitcli.dataset.consumer.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.Columns;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.operation.OperationData;

import java.util.ArrayList;
import java.util.List;

public class UpdateConsumer extends IDataSetOperationConsumer {

    public UpdateConsumer(IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        Column[] columns = this.metaData.getColumns();
        Column[] primaryKeys = this.metaData.getPrimaryKeys();
        if (primaryKeys.length == 0) {
            throw new NoPrimaryKeyException(this.metaData.getTableName());
        } else {
            StringBuilder sqlBuffer = new StringBuilder(128);
            sqlBuffer.append("update ");
            sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
            boolean firstSet = true;
            List<Column> columnList = new ArrayList<>(columns.length);
            sqlBuffer.append(" set ");

            int i;
            Column column;
            String columnName;
            for (i = 0; i < columns.length; ++i) {
                column = columns[i];
                if (Columns.getColumn(column.getColumnName(), primaryKeys) == null) {
                    if (!firstSet) {
                        sqlBuffer.append(", ");
                    }

                    firstSet = false;
                    columnName = this.getQualifiedName(null, column.getColumnName(), this.connection);
                    sqlBuffer.append(columnName);
                    sqlBuffer.append(" = ?");
                    columnList.add(column);
                }
            }

            sqlBuffer.append(" where ");

            for (i = 0; i < primaryKeys.length; ++i) {
                column = primaryKeys[i];
                if (i > 0) {
                    sqlBuffer.append(" and ");
                }

                columnName = this.getQualifiedName(null, column.getColumnName(), this.connection);
                sqlBuffer.append(columnName);
                sqlBuffer.append(" = ?");
                columnList.add(column);
            }
            return new OperationData(sqlBuffer.toString(), columnList.toArray(new Column[0]));
        }
    }
}
