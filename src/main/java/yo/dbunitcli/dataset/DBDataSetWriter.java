package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.CloseConnectionOperation;
import org.dbunit.operation.DatabaseOperation;

import java.sql.SQLException;

public class DBDataSetWriter implements IDataSetWriter {
    private final DatabaseOperation operation;
    private final IDatabaseConnection connection;

    public DBDataSetWriter(IDatabaseConnection iDatabaseConnection, String operation) {
        this.operation = Operation.valueOf(operation).op;
        this.connection = iDatabaseConnection;
    }

    @Override
    public void open(String tableName) {
        try {
            this.connection.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        DefaultDataSet iDataSet = new DefaultDataSet();
        iDataSet.addTable(new UnknownBlankToNullITable(aTable));
        try {
            this.operation.execute(this.connection, iDataSet);
        } catch (SQLException | DatabaseUnitException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void close() throws DataSetException {
        try {
             this.connection.getConnection().commit();
        } catch (SQLException e) {
            throw new DataSetException(e);
        } finally {
            try {
                this.connection.close();
            } catch (SQLException e) {
                throw new DataSetException(e);
            }
        }
    }

    enum Operation {
        INSERT(DatabaseOperation.INSERT),
        UPDATE(DatabaseOperation.UPDATE),
        DELETE(DatabaseOperation.DELETE),
        REFRESH(DatabaseOperation.REFRESH),
        CLEAN_INSERT(DatabaseOperation.CLEAN_INSERT),;
        private final DatabaseOperation op;

        Operation(DatabaseOperation op) {
            this.op = new CloseConnectionOperation(op);
        }
    }
}
