package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.operation.OperationData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.stream.IntStream;

public class RefreshOperator extends DBOperator {
    private final InsertOperator insertOperator;
    private final UpdateOperator updateOperator;
    private PreparedStatement countStatement;
    private boolean exists;

    public RefreshOperator(final IDatabaseConnection connection) {
        super(connection);
        this.insertOperator = new InsertOperator(connection);
        this.updateOperator = new UpdateOperator(connection);
    }

    @Override
    public void startDataSet() throws DataSetException {
        this.insertOperator.startDataSet();
        this.updateOperator.startDataSet();
    }

    @Override
    public void startTable(final ITableMetaData iTableMetaData) throws DataSetException {
        this.updateOperator.startTable(iTableMetaData);
        this.insertOperator.startTable(iTableMetaData);
        final OperationData queryOperationData = this.getSelectCountData();
        try {
            this.countStatement = this.connection.getConnection().prepareStatement(queryOperationData.getSql());
        } catch (final SQLException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void row(final Object[] row) throws DataSetException {
        this.exists = this.exists(row);
        if (this.exists) {
            this.updateOperator.row(row);
        } else {
            this.insertOperator.row(row);
        }
    }

    @Override
    public void endTable() throws DataSetException {
        this.updateOperator.endTable();
        this.insertOperator.endTable();
    }

    @Override
    public void endDataSet() throws DataSetException {
        this.updateOperator.endDataSet();
        this.insertOperator.endDataSet();
    }

    @Override
    protected BitSet getIgnoreMapping(final Object[] row) throws DataSetException {
        if (this.exists) {
            return this.updateOperator.getIgnoreMapping(row);
        }
        return this.insertOperator.getIgnoreMapping(row);
    }

    @Override
    protected OperationData getOperationData() throws DataSetException {
        if (this.exists) {
            return this.updateOperator.getOperationData();
        }
        return this.insertOperator.getOperationData();
    }

    private OperationData getSelectCountData() {
        final Column[] primaryKeys = this.primaryKeys();
        if (primaryKeys.length == 0) {
            throw new AssertionError(this.updateOperator.metaData.getTableName());
        } else {
            final StringBuilder sqlBuffer = new StringBuilder(128);
            sqlBuffer.append("select COUNT(1) from ");
            sqlBuffer.append(this.getQualifiedName(this.connection.getSchema(), this.updateOperator.metaData.getTableName(), this.connection));
            sqlBuffer.append(" where ");
            IntStream.range(0, primaryKeys.length).forEach(i -> {
                final Column column = primaryKeys[i];
                if (i > 0) {
                    sqlBuffer.append(" and ");
                }

                sqlBuffer.append(this.getQualifiedName(null, column.getColumnName(), this.connection));
                sqlBuffer.append(" = ?");
            });
            return new OperationData(sqlBuffer.toString(), primaryKeys);
        }
    }

    public boolean exists(final Object[] row) {
        final Column[] columns = this.primaryKeys();
        IntStream.range(0, columns.length).forEach(i -> {
            try {
                final Object value = row[this.updateOperator.metaData.getColumnIndex(columns[i].getColumnName())];
                columns[i].getDataType().setSqlValue(value, i + 1, this.countStatement);
            } catch (final DataSetException | SQLException e) {
                throw new AssertionError(e);
            }
        });
        try (final ResultSet resultSet = this.countStatement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (final SQLException e) {
            throw new AssertionError(e);
        }
    }

    private Column[] primaryKeys() {
        try {
            return this.updateOperator.metaData.getPrimaryKeys();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
