package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;

import java.sql.SQLException;

public class DBDataSetWriter implements IDataSetWriter {
    private final DatabaseOperation operation;
    private final IDatabaseConnection connection;
    private final DefaultDataSet iDataSet;

    public DBDataSetWriter(IDatabaseConnection iDatabaseConnection, String operation) {
        this.operation = Operation.valueOf(operation).op;
        this.connection = iDatabaseConnection;
        this.iDataSet = new DefaultDataSet();
    }

    @Override
    public void open(String tableName) {
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        iDataSet.addTable(new UnknownBlankToNullITable(aTable));
    }

    @Override
    public void close() throws DataSetException {
        try {
            this.operation.execute(this.connection, iDataSet);
        } catch (SQLException | DatabaseUnitException e) {
            throw new DataSetException(e);
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
            this.op = DatabaseOperation.CLOSE_CONNECTION(DatabaseOperation.TRANSACTION(op));
        }
    }
}
