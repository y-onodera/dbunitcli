package yo.dbunitcli.dataset.converter;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.DataSetConverterParam;
import yo.dbunitcli.dataset.DbOperation;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.ParallelTableMapping;
import yo.dbunitcli.dataset.converter.db.*;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.util.List;

public class DBConverter implements IDataSetConverter {

    private final IDatabaseConnection connection;
    private final DbOperation operation;
    private final boolean exportEmptyTable;
    private final DBOperator operator;
    /** 並列実行用に別の接続を張るためのloader。nullなら並列化しない */
    private final DatabaseConnectionLoader connectionLoader;
    /** 接続を自分で張ったか（closeで閉じる）。他から受け取った接続は渡した側が閉じる */
    private final boolean ownsConnection;

    public DBConverter(final DataSetConverterParam param) {
        this(param.databaseConnectionLoader().loadConnection()
                , param.operation()
                , param.exportEmptyTable()
                , param.databaseConnectionLoader()
                , true);
    }

    public DBConverter(final IDatabaseConnection connection, final DbOperation operation, final boolean exportEmptyTable) {
        this(connection, operation, exportEmptyTable, null, false);
    }

    private DBConverter(final IDatabaseConnection connection, final DbOperation operation, final boolean exportEmptyTable
            , final DatabaseConnectionLoader connectionLoader, final boolean ownsConnection) {
        this.connection = connection;
        this.operation = operation;
        this.exportEmptyTable = exportEmptyTable;
        this.operator = this.createOperator(operation, connection);
        this.connectionLoader = connectionLoader;
        this.ownsConnection = ownsConnection;
    }

    @Override
    public void close() {
        if (this.ownsConnection) {
            DatabaseConnectionLoader.close(this.connection);
        }
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

    /**
     * 読込側（同じスレッドで結果セットを読みながら書き込む）とは別の接続にするため、keyにDBConverterを含める
     */
    @Override
    public IDataSetConverter splitForParallel(final ParallelTableMapping.WorkerResources resources) {
        final IDatabaseConnection connection = resources.get(List.of(DBConverter.class, this.connectionLoader)
                , this.connectionLoader::loadConnection, IDatabaseConnection::close);
        // 接続はWorkerResourcesが作業スレッドの終了時に閉じる
        return new DBConverter(connection, this.operation, this.exportEmptyTable, this.connectionLoader, false);
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
