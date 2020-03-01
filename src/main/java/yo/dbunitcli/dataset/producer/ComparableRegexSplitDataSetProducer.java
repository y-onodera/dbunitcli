package yo.dbunitcli.dataset.producer;

import com.google.common.io.Files;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
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
import java.util.regex.Pattern;

public class ComparableRegexSplitDataSetProducer implements ComparableDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private final String encoding;
    private final Pattern dataSplitPattern;
    private final Pattern headerSplitPattern;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;

    public ComparableRegexSplitDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.encoding = this.param.getEncoding();
        this.headerSplitPattern = Pattern.compile(this.param.getHeaderSplitPattern());
        this.dataSplitPattern = Pattern.compile(this.param.getDataSplitPattern());
        this.filter = this.param.getTableNameFilter();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
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
            if (this.filter.predicate(file.getAbsolutePath())) {
                try {
                    this.executeQuery(file);
                } catch (IOException e) {
                    throw new DataSetException(e);
                }
            }
        }
        this.consumer.endDataSet();

    }

    protected void executeQuery(File aFile) throws DataSetException, IOException {
        logger.info("produceFromFile(theDataFile={}) - start", aFile);
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
