package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.*;
import org.dbunit.dataset.stream.IDataSetConsumer;
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
    private static final Logger LOGGER = LogManager.getLogger();
    protected final IDatabaseConnection connection;
    protected IDataSetConsumer consumer;
    protected final File[] src;
    protected final String encoding;
    protected final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private IDataSet databaseDataSet;
    private final boolean loadData;

    public ComparableDBDataSetProducer(final ComparableDataSetParam param) {
        this.connection = param.getDatabaseConnectionLoader().loadConnection();
        this.param = param;
        if (this.getParam().getSrc().isDirectory()) {
            this.src = this.getParam().getSrc().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.getParam().getSrc()};
        }
        this.encoding = this.param.getEncoding();
        this.filter = this.param.getTableNameFilter();
        this.loadData = this.param.isLoadData();
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
        LOGGER.info("produce() - start");
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
        LOGGER.info("produce() - end");
    }

    protected void loadJdbcMetadata() {
        if (this.getParam().isUseJdbcMetaData()) {
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
            LOGGER.info("produce - start databaseTable={}", table.getTableMetaData().getTableName());
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
                LOGGER.info("produce - rows={}", row);
            }
            this.consumer.endTable();
            LOGGER.info("produce - end   databaseTable={}", table.getTableMetaData().getTableName());
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
