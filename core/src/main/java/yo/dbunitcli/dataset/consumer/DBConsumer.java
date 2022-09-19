package yo.dbunitcli.dataset.consumer;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConsumer;
import yo.dbunitcli.dataset.consumer.db.*;

public class DBConsumer implements IDataSetConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DBConsumer.class);
    private final org.dbunit.dataset.stream.IDataSetConsumer operation;
    private final IDatabaseConnection connection;
    private final DefaultDataSet iDataSet;

    public DBConsumer(DataSetConsumerParam param) throws DataSetException {
        this.connection = param.getDatabaseConnectionLoader().loadConnection();
        this.operation = param.getOperation().createConsumer(this.connection);
        this.iDataSet = new DefaultDataSet();
    }

    @Override
    public void startDataSet() throws DataSetException {
        this.operation.startDataSet();
    }

    @Override
    public void startTable(ITableMetaData iTableMetaData) throws DataSetException {
        this.operation.startTable(iTableMetaData);
    }

    @Override
    public void row(Object[] objects) throws DataSetException {
        this.operation.row(objects);
    }

    @Override
    public void endTable() throws DataSetException {
        this.operation.endTable();
    }

    @Override
    public void endDataSet() throws DataSetException {
        this.operation.endDataSet();
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
        iDataSet.addTable(aTable);
    }

    @Override
    public void close() throws DataSetException {
        logger.info("execute DBOperation {} - start", this.operation);
        IDataSetProducer producer = new DataSetProducerAdapter(this.iDataSet);
        producer.setConsumer(this);
        producer.produce();
    }

    public enum Operation {
        INSERT,
        UPDATE,
        DELETE,
        REFRESH,
        CLEAN_INSERT;

        org.dbunit.dataset.stream.IDataSetConsumer createConsumer(IDatabaseConnection connection) throws DataSetException {
            switch (this) {
                case INSERT:
                    return new InsertConsumer(connection);
                case UPDATE:
                    return new UpdateConsumer(connection);
                case DELETE:
                    return new DeleteConsumer(connection);
                case REFRESH:
                    return new RefreshConsumer(connection);
            }
            return new CleanInsertConsumer(connection);
        }
    }
}