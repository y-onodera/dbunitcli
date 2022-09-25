package yo.dbunitcli.dataset.converter;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.converter.db.*;

public class DBConverter implements IDataSetConverter {
    private final org.dbunit.dataset.stream.IDataSetConsumer operation;

    private final boolean exportEmptyTable;

    public DBConverter(DataSetConsumerParam param) throws DataSetException {
        IDatabaseConnection connection = param.getDatabaseConnectionLoader().loadConnection();
        this.operation = param.getOperation().createConsumer(connection);
        this.exportEmptyTable = param.isExportEmptyTable();
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
    public void reStartTable(ITableMetaData tableMetaData, Integer writeRows) throws DataSetException {
        this.operation.startTable(tableMetaData);
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
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
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