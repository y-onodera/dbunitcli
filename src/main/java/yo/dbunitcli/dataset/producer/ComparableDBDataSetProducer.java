package yo.dbunitcli.dataset.producer;

import com.google.common.io.Files;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.*;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ComparableDBDataSetProducer.class);
    protected final IDatabaseConnection connection;
    protected IDataSetConsumer consumer = new DefaultConsumer();
    protected final File[] src;
    protected final String encoding;
    protected final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private IDataSet databaseDataSet;
    private final boolean loadData;

    public ComparableDBDataSetProducer(IDatabaseConnection connection, ComparableDataSetParam param) {
        this.connection = connection;
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
        logger.info("produce() - start");
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
                        final SortedTable table = new SortedTable(originTable);
                        table.setUseComparable(true);
                        this.executeTable(table);
                    } catch (SQLException | DataSetException e) {
                        throw new AssertionError(e);
                    }
                });
        this.consumer.endDataSet();
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
        logger.info("produceFromDB(table={}) - start", table.getTableMetaData().getTableName());
        this.consumer.startTable(table.getTableMetaData());
        if (this.loadData) {
            Column[] columns = table.getTableMetaData().getColumns();
            for (int row = 0, j = table.getRowCount(); row < j; row++) {
                Object[] rows = new Object[columns.length];
                int columnIndex = 0;
                for (Column column : columns) {
                    rows[columnIndex++] = table.getValue(row, column.getColumnName());
                }
                this.consumer.row(rows);
            }
        }
        this.consumer.endTable();
    }
}
