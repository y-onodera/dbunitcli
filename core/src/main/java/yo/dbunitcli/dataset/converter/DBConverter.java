package yo.dbunitcli.dataset.converter;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.converter.db.*;

public class DBConverter implements IDataSetConverter {

    private final IDatabaseConnection connection;
    private final Operation operation;
    private final boolean exportEmptyTable;
    private final DBOperator operator;

    public DBConverter(final DataSetConverterParam param) {
        this(param.databaseConnectionLoader().loadConnection()
                , param.operation()
                , param.exportEmptyTable());
    }

    public DBConverter(final IDatabaseConnection connection, final Operation operation, final boolean exportEmptyTable) {
        this.connection = connection;
        this.operation = operation;
        this.exportEmptyTable = exportEmptyTable;
        this.operator = operation.operator(connection);
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
        this.operator.reStartTable(tableMetaData);
    }

    @Override
    public IDataSetConverter split() {
        try {
            final IDataSetConverter result = new DBConverter(this.connection, this.operation, this.exportEmptyTable);
            result.startDataSet();
            return result;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void startDataSet() throws DataSetException {
        this.operator.startDataSet();
    }

    @Override
    public void endDataSet() throws DataSetException {
        this.operator.endDataSet();
    }

    @Override
    public void startTable(final ITableMetaData iTableMetaData) throws DataSetException {
        this.operator.startTable(iTableMetaData);
    }

    @Override
    public void endTable() throws DataSetException {
        this.operator.endTable();
    }

    @Override
    public void row(final Object[] objects) throws DataSetException {
        this.operator.row(objects);
    }

    public enum Operation {
        INSERT,
        UPDATE,
        DELETE,
        REFRESH,
        CLEAN_INSERT;

        DBOperator operator(final IDatabaseConnection connection) {
            return switch (this) {
                case INSERT -> new InsertOperator(connection);
                case UPDATE -> new UpdateOperator(connection);
                case DELETE -> new DeleteOperator(connection);
                case REFRESH -> new RefreshOperator(connection);
                default -> new CleanInsertOperator(connection);
            };
        }
    }
}