package yo.dbunitcli.dataset.consumer.db;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.dbunit.operation.AbstractOperation;
import org.dbunit.operation.OperationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public abstract class IDataSetOperationConsumer extends AbstractOperation implements IDataSetConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IDataSetOperationConsumer.class);
    private static final BitSet EMPTY_BITSET = new BitSet();
    protected final IDatabaseConnection connection;
    protected IStatementFactory factory;
    protected boolean allowEmptyFields;
    protected OperationData operationData;
    protected IPreparedBatchStatement statement;
    protected ITableMetaData metaData;
    protected BitSet ignoreMapping;

    public IDataSetOperationConsumer(IDatabaseConnection connection)  {
        this.connection = connection;
    }

    @Override
    public void execute(IDatabaseConnection iDatabaseConnection, IDataSet dataSet) throws DatabaseUnitException, SQLException {
        IDataSetProducer producer = new DataSetProducerAdapter(dataSet);
        producer.setConsumer(this);
        producer.produce();
    }

    @Override
    public void startDataSet() throws DataSetException {
        DatabaseConfig databaseConfig = this.connection.getConfig();
        this.factory = (IStatementFactory) databaseConfig.getProperty("http://www.dbunit.org/properties/statementFactory");
        this.allowEmptyFields = connection.getConfig().getFeature("http://www.dbunit.org/features/allowEmptyFields");
    }

    @Override
    public void startTable(ITableMetaData iTableMetaData) throws DataSetException {
        try {
            metaData = this.getOperationMetaData(iTableMetaData);
        } catch (DatabaseUnitException | SQLException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void row(Object[] row) throws DataSetException {
        try {
            if (this.ignoreMapping == null || !this.equalsIgnoreMapping(this.ignoreMapping, row)) {
                if (this.statement != null) {
                    this.statement.executeBatch();
                    this.statement.clearBatch();
                    this.statement.close();
                }
                this.ignoreMapping = this.getIgnoreMapping(row);
                this.operationData = this.getOperationData();
                this.statement = this.factory.createPreparedBatchStatement(this.operationData.getSql(), this.connection);
            }
            Column[] columns = this.operationData.getColumns();

            for (int j = 0; j < columns.length; ++j) {
                if (!this.ignoreMapping.get(j)) {
                    Column column = columns[j];
                    String columnName = column.getColumnName();

                    try {
                        DataType dataType = column.getDataType();
                        Object value = row[this.metaData.getColumnIndex(columnName)];
                        if ("".equals(value)) {
                            if (!this.allowEmptyFields) {
                                this.handleColumnHasNoValue(this.metaData.getTableName(), columnName);
                            } else {
                                value = null;
                            }
                        }

                        this.statement.addValue(value, dataType);
                    } catch (TypeCastException var28) {
                        String msg = "Error casting value for table '" + this.metaData.getTableName() + "' and column '" + columnName + "'";
                        LOGGER.error("execute: {}", msg);
                        throw new TypeCastException(msg, var28);
                    }
                }
            }
            this.statement.addBatch();
        } catch (SQLException var30) {
            String msg = "Exception processing table name='" + this.metaData.getTableName() + "'";
            if (this.statement != null) {
                try {
                    this.statement.close();
                    this.statement = null;
                } catch (SQLException e) {
                    throw new DataSetException(e);
                }
                throw new DataSetException(msg, var30);
            }
        }
    }

    @Override
    public void endTable() throws DataSetException {
        this.ignoreMapping = null;
        this.operationData = null;
        if (this.statement != null) {
            try {
                this.statement.executeBatch();
                this.statement.clearBatch();
                this.statement.close();
                this.statement = null;
            } catch (SQLException e) {
                throw new DataSetException(e);
            }
        }
    }

    @Override
    public void endDataSet() throws DataSetException {

    }

    protected BitSet getIgnoreMapping(Object[] row) throws DataSetException {
        return EMPTY_BITSET;
    }

    protected boolean equalsIgnoreMapping(BitSet ignoreMapping, Object[] row) throws DataSetException {
        return true;
    }

    protected abstract OperationData getOperationData() throws DataSetException;

    protected ITableMetaData getOperationMetaData(ITableMetaData metaData) throws DatabaseUnitException, SQLException {
        LOGGER.debug("getOperationMetaData(connection={}, metaData={}) - start", this.connection, metaData);
        IDataSet databaseDataSet = this.connection.createDataSet();
        String tableName = metaData.getTableName();
        ITableMetaData tableMetaData = databaseDataSet.getTableMetaData(tableName);
        Column[] columns = metaData.getColumns();
        List<Column> columnList = new ArrayList<>();

        for (Column column : columns) {
            String columnName = column.getColumnName();
            int dbColIndex = tableMetaData.getColumnIndex(columnName);
            Column dbColumn = tableMetaData.getColumns()[dbColIndex];
            columnList.add(dbColumn);
        }

        return new DefaultTableMetaData(tableMetaData.getTableName(), columnList.toArray(new Column[0]), tableMetaData.getPrimaryKeys());
    }

    protected void handleColumnHasNoValue(String tableName, String columnName) {
        String tableColumnName = tableName + "." + columnName;
        String msg = "table.column=" + tableColumnName + " value is empty but must contain a value (to disable this feature check, set DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS to true)";
        LOGGER.error("execute: {}", msg);
        throw new IllegalArgumentException(msg);
    }

}
