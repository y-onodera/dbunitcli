package yo.dbunitcli.dataset.consumer.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoPrimaryKeyException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.operation.OperationData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;

public class RefreshConsumer extends IDataSetOperationConsumer {
    private final InsertConsumer insertConsumer;
    private final UpdateConsumer updateConsumer;
    private PreparedStatement countStatement;
    private boolean exists;

    public RefreshConsumer(IDatabaseConnection connection) {
        super(connection);
        this.insertConsumer = new InsertConsumer(connection);
        this.updateConsumer = new UpdateConsumer(connection);
    }

    @Override
    public void startDataSet() throws DataSetException {
        this.insertConsumer.startDataSet();
        this.updateConsumer.startDataSet();
    }

    @Override
    public void startTable(ITableMetaData iTableMetaData) throws DataSetException {
        this.updateConsumer.startTable(iTableMetaData);
        this.insertConsumer.startTable(iTableMetaData);
        OperationData queryOperationData = this.getSelectCountData();
        try {
            this.countStatement = connection.getConnection().prepareStatement(queryOperationData.getSql());
        } catch (SQLException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void row(Object[] row) throws DataSetException {
        try {
            this.exists = this.exists(row);
        } catch (SQLException e) {
            throw new DataSetException(e);
        }
        if (this.exists) {
            this.updateConsumer.row(row);
        } else {
            this.insertConsumer.row(row);
        }
    }

    @Override
    public void endTable() throws DataSetException {
        this.updateConsumer.endTable();
        this.insertConsumer.endTable();
    }

    @Override
    public void endDataSet() throws DataSetException {
        this.updateConsumer.endDataSet();
        this.insertConsumer.endDataSet();
    }

    @Override
    protected BitSet getIgnoreMapping(Object[] row) throws DataSetException {
        if (this.exists) {
            return this.updateConsumer.getIgnoreMapping(row);
        }
        return this.insertConsumer.getIgnoreMapping(row);
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        if (this.exists) {
            return this.updateConsumer.getOperationData();
        }
        return this.insertConsumer.getOperationData();
    }

    private OperationData getSelectCountData() throws DataSetException {
        Column[] primaryKeys = this.updateConsumer.metaData.getPrimaryKeys();
        if (primaryKeys.length == 0) {
            throw new NoPrimaryKeyException(this.updateConsumer.metaData.getTableName());
        } else {
            StringBuilder sqlBuffer = new StringBuilder(128);
            sqlBuffer.append("select COUNT(1) from ");
            sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.updateConsumer.metaData.getTableName(), this.connection));
            sqlBuffer.append(" where ");

            for (int i = 0; i < primaryKeys.length; ++i) {
                Column column = primaryKeys[i];
                if (i > 0) {
                    sqlBuffer.append(" and ");
                }

                sqlBuffer.append(this.getQualifiedName(null, column.getColumnName(), this.connection));
                sqlBuffer.append(" = ?");
            }
            return new OperationData(sqlBuffer.toString(), primaryKeys);
        }
    }

    public boolean exists(Object[] row) throws DataSetException, SQLException {
        Column[] columns = this.updateConsumer.metaData.getPrimaryKeys();
        for (int i = 0; i < columns.length; ++i) {
            Object value = row[this.updateConsumer.metaData.getColumnIndex(columns[i].getColumnName())];
            DataType dataType = columns[i].getDataType();
            dataType.setSqlValue(value, i + 1, this.countStatement);
        }
        try (ResultSet resultSet = this.countStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1) > 0;
        }
    }

}
