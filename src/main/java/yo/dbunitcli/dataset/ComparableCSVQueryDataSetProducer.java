package yo.dbunitcli.dataset;

import com.google.common.io.Files;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;

public class ComparableCSVQueryDataSetProducer implements IDataSetProducer {

    private static final Logger logger = LoggerFactory.getLogger(ComparableCsvDataSetProducer.class);
    private static final String URL = "jdbc:h2:mem:h2test";
    private IDataSetConsumer consumer = new DefaultConsumer();
    private File[] src;
    private String encoding = System.getProperty("file.encoding");

    public ComparableCSVQueryDataSetProducer(File srcDir, String encoding) throws DataSetException {
        if (!srcDir.isDirectory()) {
            throw new DataSetException("'" + srcDir + "' should be a directory");
        }
        this.src = srcDir.listFiles();
        this.encoding = encoding;
    }

    public ComparableCSVQueryDataSetProducer(File aSrcFile) throws DataSetException {
        if (!aSrcFile.isFile()) {
            throw new DataSetException("'" + aSrcFile.toString() + "' should be a file");
        }
        this.src = new File[]{aSrcFile};
    }

    @Override
    public void setConsumer(IDataSetConsumer iDataSetConsumer) throws DataSetException {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.debug("produce() - start");

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

    private void executeQuery(File aFile) throws SQLException, DataSetException, IOException {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rst = stmt.executeQuery(Files.asCharSource(aFile, Charset.forName(this.encoding)).read());
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
}
