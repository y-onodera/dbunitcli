package yo.dbunitcli.dataset;

import com.google.common.io.Files;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;

public class ComparableDBDataSetProducer implements IDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableDBDataSetProducer.class);
    protected IDatabaseConnection connection;
    protected IDataSetConsumer consumer = new DefaultConsumer();
    protected File src;
    protected String encoding = System.getProperty("file.encoding");

    public ComparableDBDataSetProducer(IDatabaseConnection connection, File src, String encoding) throws DataSetException {
        this.connection = connection;
        this.src = src;
        this.encoding = encoding;
    }

    @Override
    public void setConsumer(IDataSetConsumer iDataSetConsumer) throws DataSetException {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.debug("produce() - start");
        this.consumer.startDataSet();
        try {
            for (String tableName : Files.readLines(this.src, Charset.forName(this.encoding))) {
                final SortedTable table = new SortedTable(this.connection.createTable(tableName));
                table.setUseComparable(true);
                this.executeTable(table);
            }
        } catch (SQLException | IOException e) {
            throw new DataSetException(e);
        }
        this.consumer.endDataSet();
    }

    protected void executeTable(ITable table) throws DataSetException {
        this.consumer.startTable(table.getTableMetaData());
        Column[] columns = table.getTableMetaData().getColumns();
        for (int row = 0, j = table.getRowCount(); row < j; row++) {
            Object[] rows = new Object[columns.length];
            int columnIndex = 0;
            for (Column column : columns) {
                rows[columnIndex++] = table.getValue(row, column.getColumnName());
            }
            this.consumer.row(rows);
        }
        this.consumer.endTable();
    }
}
