package yo.dbunitcli.dataset.converter;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.converter.db.*;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.sql.SQLException;

public class DBConverter implements IDataSetConverter {

    private final IDatabaseConnection connection;
    private final DbOperation operation;
    private final boolean exportEmptyTable;
    private final DBOperator operator;
    /** 並列実行用に別の接続を張るためのloader。nullなら並列化しない */
    private final DatabaseConnectionLoader connectionLoader;
    /** 接続をこのインスタンスで張った（release()で閉じる）か */
    private final boolean ownConnection;

    public DBConverter(final DataSetConverterParam param) {
        this(param.databaseConnectionLoader().loadConnection()
                , param.operation()
                , param.exportEmptyTable()
                , param.databaseConnectionLoader()
                , false);
    }

    public DBConverter(final IDatabaseConnection connection, final DbOperation operation, final boolean exportEmptyTable) {
        this(connection, operation, exportEmptyTable, null, false);
    }

    private DBConverter(final IDatabaseConnection connection, final DbOperation operation, final boolean exportEmptyTable
            , final DatabaseConnectionLoader connectionLoader, final boolean ownConnection) {
        this.connection = connection;
        this.operation = operation;
        this.exportEmptyTable = exportEmptyTable;
        this.operator = this.createOperator(operation, connection);
        this.connectionLoader = connectionLoader;
        this.ownConnection = ownConnection;
    }

    private DBOperator createOperator(final DbOperation operation, final IDatabaseConnection connection) {
        return switch (operation) {
            case INSERT -> new InsertOperator(connection);
            case UPDATE -> new UpdateOperator(connection);
            case DELETE -> new DeleteOperator(connection);
            case REFRESH -> new RefreshOperator(connection);
            case CLEAN_INSERT -> new CleanInsertOperator(connection);
        };
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {
        this.operator.reStartTable(tableMetaData);
    }

    /**
     * JDBC接続は複数スレッドから同時に使えないため、並列実行ではスレッドごとに接続を張る
     */
    @Override
    public boolean isParallelizable() {
        return this.connectionLoader != null;
    }

    @Override
    public IDataSetConverter splitForParallel() {
        return new DBConverter(this.connectionLoader.loadConnection(), this.operation, this.exportEmptyTable
                , this.connectionLoader, true);
    }

    @Override
    public void release() {
        if (this.ownConnection) {
            try {
                this.connection.close();
            } catch (final SQLException e) {
                throw new AssertionError(e);
            }
        }
    }

    @Override
    public IDataSetConverter split() {
            final IDataSetConverter result = new DBConverter(this.connection, this.operation, this.exportEmptyTable);
            result.startDataSet();
            return result;
    }

    @Override
    public void startDataSet() {
        try {
        this.operator.startDataSet();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void endDataSet() {
        try {
        this.operator.endDataSet();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
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

}
