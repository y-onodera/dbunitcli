package yo.dbunitcli.dataset.writer;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.sql.SQLException;

public class DBDataSetWriter implements IDataSetWriter {
    private static final Logger logger = LoggerFactory.getLogger(DBDataSetWriter.class);
    private final DatabaseOperation operation;
    private final IDatabaseConnection connection;
    private final DefaultDataSet iDataSet;

    public DBDataSetWriter(DataSetWriterParam param) throws DataSetException {
        this.operation = param.getOperation().op;
        this.connection = param.getDatabaseConnectionLoader().loadConnection();
        this.iDataSet = new DefaultDataSet();
    }

    @Override
    public void cleanupDirectory() {
    }

    @Override
    public void open(String tableName) {
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        logger.info("addTable {}", aTable.getTableMetaData().getTableName());
        iDataSet.addTable(new UnknownBlankToNullITable(aTable));
    }

    @Override
    public void close() throws DataSetException {
        logger.info("execute DBOperation {} - start", this.operation);
        try {
            this.operation.execute(this.connection, iDataSet);
        } catch (SQLException | DatabaseUnitException e) {
            throw new DataSetException(e);
        }
    }

    public enum Operation {
        INSERT(DatabaseOperation.INSERT),
        UPDATE(DatabaseOperation.UPDATE),
        DELETE(DatabaseOperation.DELETE),
        REFRESH(DatabaseOperation.REFRESH),
        CLEAN_INSERT(new CompositeOperation(DatabaseOperation.TRUNCATE_TABLE, DatabaseOperation.INSERT)),
        ;
        private final DatabaseOperation op;

        Operation(DatabaseOperation op) {
            this.op = DatabaseOperation.CLOSE_CONNECTION(DatabaseOperation.TRANSACTION(op));
        }
    }
}
