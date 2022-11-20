package yo.dbunitcli.dataset.converter.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.statement.IPreparedBatchStatement;
import org.dbunit.database.statement.IStatementFactory;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.dbunit.operation.AbstractOperation;
import org.dbunit.operation.OperationData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

public abstract class IDataSetOperationConsumer extends AbstractOperation implements IDataSetConsumer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final BitSet EMPTY_BITSET = new BitSet();
    protected final IDatabaseConnection connection;
    protected IStatementFactory factory;
    protected boolean allowEmptyFields;
    protected OperationData operationData;
    protected IPreparedBatchStatement statement;
    protected ITableMetaData metaData;
    protected BitSet ignoreMapping;
    protected int writeRows;

    public IDataSetOperationConsumer(final IDatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public void execute(final IDatabaseConnection iDatabaseConnection, final IDataSet dataSet) throws DatabaseUnitException, SQLException {
        final IDataSetProducer producer = new DataSetProducerAdapter(dataSet);
        producer.setConsumer(this);
        producer.produce();
    }

    @Override
    public void startDataSet() throws DataSetException {
        final DatabaseConfig databaseConfig = this.connection.getConfig();
        this.factory = (IStatementFactory) databaseConfig.getProperty("http://www.dbunit.org/properties/statementFactory");
        this.allowEmptyFields = (Boolean) databaseConfig.getProperty("http://www.dbunit.org/features/allowEmptyFields");
    }

    @Override
    public void startTable(final ITableMetaData iTableMetaData) throws DataSetException {
        try {
            this.metaData = this.getOperationMetaData(iTableMetaData);
            this.writeRows = 0;
            LOGGER.info("convert - start databaseTable={},className={}", this.metaData.getTableName(), this.getClass().getSimpleName());
        } catch (final DatabaseUnitException | SQLException e) {
            throw new DataSetException(e);
        }
    }

    @Override
    public void row(final Object[] row) throws DataSetException {
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
            final Column[] columns = this.operationData.getColumns();

            IntStream.range(0, columns.length).forEach(j -> {
                if (!this.ignoreMapping.get(j)) {
                    final Column column = columns[j];
                    final String columnName = column.getColumnName();

                    try {
                        final DataType dataType = column.getDataType();
                        Object value = row[this.getColumnIndex(columnName)];
                        if ("".equals(value)) {
                            if (!this.allowEmptyFields) {
                                this.handleColumnHasNoValue(this.metaData.getTableName(), columnName);
                            } else {
                                value = null;
                            }
                        }

                        this.statement.addValue(value, dataType);
                    } catch (final SQLException | DataSetException var28) {
                        final String msg = "Error casting value for table '" + this.metaData.getTableName() + "' and column '" + columnName + "'";
                        LOGGER.error("execute: {}", msg);
                        throw new AssertionError(msg, var28);
                    }
                }
            });
            this.statement.addBatch();
            this.writeRows++;
        } catch (final SQLException var30) {
            final String msg = "Exception processing table name='" + this.metaData.getTableName() + "'";
            if (this.statement != null) {
                try {
                    this.statement.close();
                    this.statement = null;
                } catch (final SQLException e) {
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
            } catch (final SQLException e) {
                throw new DataSetException(e);
            }
        }
        LOGGER.info("convert - rows={},className={}", this.writeRows, this.getClass().getSimpleName());
        LOGGER.info("convert - end   databaseTable={},className={}", this.metaData.getTableName(), this.getClass().getSimpleName());
    }

    @Override
    public void endDataSet() throws DataSetException {

    }

    protected BitSet getIgnoreMapping(final Object[] row) throws DataSetException {
        return EMPTY_BITSET;
    }

    protected boolean equalsIgnoreMapping(final BitSet ignoreMapping, final Object[] row) throws DataSetException {
        return true;
    }

    protected abstract OperationData getOperationData() throws DataSetException;

    protected ITableMetaData getOperationMetaData(final ITableMetaData metaData) throws DatabaseUnitException, SQLException {
        LOGGER.debug("getOperationMetaData(connection={}, metaData={}) - start", this.connection, metaData);
        final IDataSet databaseDataSet = this.connection.createDataSet();
        final String tableName = metaData.getTableName();
        final ITableMetaData tableMetaData = databaseDataSet.getTableMetaData(tableName);
        final Column[] columns = metaData.getColumns();
        final List<Column> columnList = new ArrayList<>();
        Arrays.stream(columns).forEach(column -> {
            try {
                columnList.add(tableMetaData.getColumns()[tableMetaData.getColumnIndex(column.getColumnName())]);
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        });
        return new DefaultTableMetaData(tableMetaData.getTableName(), columnList.toArray(new Column[0]), tableMetaData.getPrimaryKeys());
    }

    protected int getColumnIndex(final String columnName) {
        try {
            return this.metaData.getColumnIndex(columnName);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void handleColumnHasNoValue(final String tableName, final String columnName) {
        final String tableColumnName = tableName + "." + columnName;
        final String msg = "table.column=" + tableColumnName + " value is empty but must contain a value (to disable this feature check, set DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS to true)";
        LOGGER.error("execute: {}", msg);
        throw new IllegalArgumentException(msg);
    }

}
