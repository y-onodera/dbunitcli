package yo.dbunitcli.dataset.producer;

import com.google.common.io.Files;
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

    public ComparableDBDataSetProducer(ComparableDataSetParam param) throws DataSetException {
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
    public void setConsumer(IDataSetConsumer iDataSetConsumer) {
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
                        return Files.readLines(it, Charset.forName(this.encoding));
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                })
                .flatMap(Collection::stream)
                .filter(this.filter::predicate)
                .distinct()
                .forEach(tableName -> {
                    try {
                        ITable originTable;
                        if (this.databaseDataSet != null) {
                            originTable = this.databaseDataSet.getTable(tableName);
                        } else {
                            originTable = this.connection.createTable(tableName);
                        }
                        this.executeTable(originTable);
                    } catch (SQLException | DataSetException e) {
                        throw new AssertionError(e);
                    }
                });
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    protected void loadJdbcMetadata() {
        if (this.getParam().isUseJdbcMetaData()) {
            try {
                this.databaseDataSet = connection.createDataSet();
            } catch (SQLException e) {
                throw new AssertionError(e);
            }
        }
    }

    protected void executeTable(ITable table) throws DataSetException {
        LOGGER.info("produce - start databaseTable={}", table.getTableMetaData().getTableName());
        this.consumer.startTable(table.getTableMetaData());
        if (this.loadData) {
            Column[] columns = table.getTableMetaData().getColumns();
            int row = 0;
            for (; true; row++) {
                try {
                    Object[] rows = new Object[columns.length];
                    int columnIndex = 0;
                    for (Column column : columns) {
                        rows[columnIndex++] = table.getValue(row, column.getColumnName());
                    }
                    this.consumer.row(rows);
                } catch (RowOutOfBoundsException e) {
                    break;
                }
            }
            LOGGER.info("produce - rows={}", row);
        }
        this.consumer.endTable();
        LOGGER.info("produce - end   databaseTable={}", table.getTableMetaData().getTableName());
    }
}
