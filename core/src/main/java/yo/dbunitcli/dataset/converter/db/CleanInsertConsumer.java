package yo.dbunitcli.dataset.converter.db;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IBatchStatement;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.sql.SQLException;

public class CleanInsertConsumer extends InsertConsumer {
    public CleanInsertConsumer(final IDatabaseConnection connection) {
        super(connection);
    }

    @Override
    public void startTable(final ITableMetaData iTableMetaData) throws DataSetException {
        super.startTable(iTableMetaData);
        try {
            final IBatchStatement statement = this.factory.createBatchStatement(this.connection);
            statement.addBatch("truncate table " +
                    this.getQualifiedName(this.connection.getSchema(), this.metaData.getTableName(), this.connection));
            statement.executeBatch();
            statement.clearBatch();
        } catch (final SQLException e) {
            throw new DataSetException(e);
        }
    }
}
