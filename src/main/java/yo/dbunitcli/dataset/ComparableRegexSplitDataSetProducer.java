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
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

public class ComparableRegexSplitDataSetProducer implements IDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private String encoding = System.getProperty("file.encoding");
    private final Pattern dataSplitPattern;
    private final Pattern headerSplitPattern;

    public ComparableRegexSplitDataSetProducer(String headerRegex, String regex, File srcDir, String aEncoding) throws DataSetException {
        if (!srcDir.isDirectory()) {
            throw new DataSetException("'" + srcDir + "' should be a directory");
        }
        this.src = srcDir.listFiles(File::isFile);
        this.encoding = aEncoding;
        this.headerSplitPattern = Pattern.compile(headerRegex);
        this.dataSplitPattern = Pattern.compile(regex);
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
            } catch (IOException e) {
                throw new DataSetException(e);
            }
        }
        this.consumer.endDataSet();

    }

    protected void executeQuery(File aFile) throws DataSetException, IOException {
        int lineNum = 0;
        for (String s : Files.readLines(aFile, Charset.forName(this.getEncoding()))) {
            if (lineNum == 0) {
                String[] header = headerSplitPattern.split(s);
                Column[] columns = new Column[header.length];

                for (int i = 0; i < header.length; i++) {
                    String columnName = header[i];
                    columnName = columnName.trim();
                    columns[i] = new Column(columnName, DataType.UNKNOWN);
                }
                String tableName = aFile.getName().substring(0, aFile.getName().indexOf("."));
                ITableMetaData tableMetaData = new DefaultTableMetaData(tableName, columns);
                this.consumer.startTable(tableMetaData);
            } else {
                Object[] row = this.dataSplitPattern.split(s);
                this.consumer.row(row);
            }
            lineNum++;
        }
        this.consumer.endTable();
    }

    public String getEncoding() {
        return this.encoding;
    }

}
