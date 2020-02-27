package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.application.Parameter;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;

public class ComparableCSVQueryDataSetProducer implements IDataSetProducer, QueryReader {

    private static final Logger logger = LoggerFactory.getLogger(ComparableCSVQueryDataSetProducer.class);
    private static final String URL = "jdbc:h2:mem:test;ALIAS_COLUMN_NAME=TRUE";
    private IDataSetConsumer consumer = new DefaultConsumer();
    private File[] src;
    private String encoding = System.getProperty("file.encoding");
    private final Parameter parameter;
    private final TableNameFilter filter;

    public ComparableCSVQueryDataSetProducer(ComparableDataSetLoaderParam param, Parameter parameter) throws DataSetException {
        if (!param.getSrc().isDirectory()) {
            throw new DataSetException("'" + param.getSrc() + "' should be a directory");
        }
        this.src = param.getSrc().listFiles(File::isFile);
        this.encoding = param.getEncoding();
        this.filter = param.getTableNameFilter();
        this.parameter = parameter;
    }

    @Override
    public void setConsumer(IDataSetConsumer iDataSetConsumer) throws DataSetException {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.info("produce() - start");

        this.consumer.startDataSet();
        for (File file : this.src) {
            try {
                this.executeQuery(file);
            } catch (SQLException | IOException e) {
                throw new DataSetException(e);
            }
        }
        this.consumer.endDataSet();

    }

    protected void executeQuery(File aFile) throws SQLException, DataSetException, IOException {
        String query = this.readQuery(aFile);
        logger.info("produceFromQuery(query={}) - start", query);
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rst = stmt.executeQuery(query)
        ) {
            ResultSetMetaData metaData = rst.getMetaData();
            Column[] columns = new Column[metaData.getColumnCount()];

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                columnName = columnName.trim();
                columns[i - 1] = new Column(columnName, DataType.UNKNOWN);
            }
            String tableName = aFile.getName().substring(0, aFile.getName().indexOf("."));
            ITableMetaData tableMetaData = new DefaultTableMetaData(tableName, columns);
            this.consumer.startTable(tableMetaData);
            while (rst.next()) {
                Object[] row = new Object[metaData.getColumnCount()];
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (rst.getString(i) != null) {
                        row[i - 1] = rst.getString(i);
                    } else {
                        row[i - 1] = "";
                    }
                }
                this.consumer.row(row);
            }
            this.consumer.endTable();
        }
    }

    @Override
    public Map<String, Object> getParameter() {
        return this.parameter.getMap();
    }

    @Override
    public String getEncoding() {
        return this.encoding;
    }

}
