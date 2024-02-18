package yo.dbunitcli.dataset.producer;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.dataset.*;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Stream;

public class ComparableDBDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableDBDataSetProducer.class);
    protected final IDatabaseConnection connection;
    protected final File[] src;
    protected final String encoding;
    protected final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;
    protected IDataSetConsumer consumer;
    private IDataSet databaseDataSet;

    public ComparableDBDataSetProducer(final ComparableDataSetParam param) {
        this.connection = param.databaseConnectionLoader().loadConnection();
        this.param = param;
        if (this.getParam().src().isDirectory()) {
            this.src = this.getParam().src().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.getParam().src()};
        }
        this.encoding = this.param.encoding();
        this.filter = this.param.tableNameFilter();
        this.loadData = this.param.loadData();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(final IDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableDBDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        this.loadJdbcMetadata();
        Stream.of(this.src)
                .map(it -> {
                    try {
                        return Files.readAllLines(it.toPath(), Charset.forName(this.encoding));
                    } catch (final IOException e) {
                        throw new AssertionError(e);
                    }
                })
                .flatMap(Collection::stream)
                .filter(this.filter::predicate)
                .distinct()
                .forEach(this::executeTable);
        this.consumer.endDataSet();
        ComparableDBDataSetProducer.LOGGER.info("produce() - end");
    }

    protected void loadJdbcMetadata() {
        if (this.getParam().useJdbcMetaData()) {
            try {
                this.databaseDataSet = this.connection.createDataSet();
            } catch (final SQLException e) {
                throw new AssertionError(e);
            }
        }
    }

    protected void executeTable(final String tableName) {
        this.executeTable(this.getTable(tableName));
    }

    protected void executeTable(final ITable table) {
        try {
            ComparableDBDataSetProducer.LOGGER.info("produce - start databaseTable={}", table.getTableMetaData().getTableName());
            this.consumer.startTable(table.getTableMetaData());
            if (this.loadData) {
                final Column[] columns = table.getTableMetaData().getColumns();
                int row = 0;
                for (; true; row++) {
                    try {
                        final Object[] rows = new Object[columns.length];
                        int columnIndex = 0;
                        for (final Column column : columns) {
                            rows[columnIndex++] = table.getValue(row, column.getColumnName());
                        }
                        this.consumer.row(rows);
                    } catch (final RowOutOfBoundsException e) {
                        break;
                    }
                }
                ComparableDBDataSetProducer.LOGGER.info("produce - rows={}", row);
            }
            this.consumer.endTable();
            ComparableDBDataSetProducer.LOGGER.info("produce - end   databaseTable={}", table.getTableMetaData().getTableName());
            if (table instanceof IResultSetTable resultSetTable) {
                resultSetTable.close();
            }
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private ITable getTable(final String tableName) {
        try {
            if (this.databaseDataSet != null) {
                return this.databaseDataSet.getTable(tableName);
            }
            return this.connection.createTable(tableName);
        } catch (final DataSetException | SQLException e) {
            throw new AssertionError(e);
        }
    }
}
