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

    public DBConverter(final DataSetConsumerParam param) {
        final IDatabaseConnection connection = param.getDatabaseConnectionLoader().loadConnection();
        this.operation = param.getOperation().createConsumer(connection);
        this.exportEmptyTable = param.isExportEmptyTable();
    }

    @Override
    public void startDataSet() throws DataSetException {
        this.operation.startDataSet();
    }

    @Override
    public void startTable(final ITableMetaData iTableMetaData) throws DataSetException {
        this.operation.startTable(iTableMetaData);
    }

    @Override
    public void reStartTable(final ITableMetaData tableMetaData, final Integer writeRows) {
        try {
            this.operation.startTable(tableMetaData);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void row(final Object[] objects) throws DataSetException {
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
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    public enum Operation {
        INSERT,
        UPDATE,
        DELETE,
        REFRESH,
        CLEAN_INSERT;

        org.dbunit.dataset.stream.IDataSetConsumer createConsumer(final IDatabaseConnection connection) {
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