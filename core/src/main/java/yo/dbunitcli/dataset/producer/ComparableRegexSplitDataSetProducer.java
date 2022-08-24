package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
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
    private String[] headerNames;
    private Pattern headerSplitPattern;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;

    public ComparableRegexSplitDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.encoding = this.param.getEncoding();
        String headerName = this.param.getHeaderName();
        if (!Strings.isNullOrEmpty(headerName)) {
            this.headerNames = headerName.split(",");
        }
        if (this.headerNames == null) {
            this.headerSplitPattern = Pattern.compile(this.param.getHeaderSplitPattern());
        }
        this.dataSplitPattern = Pattern.compile(this.param.getDataSplitPattern());
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
        for (File file : this.src) {
            if (this.filter.predicate(file.getAbsolutePath()) && file.length() > 0) {
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
        if (this.headerNames != null) {
            ITableMetaData tableMetaData = this.createMetaData(aFile, this.headerNames);
            this.consumer.startTable(tableMetaData);
            if (!this.loadData) {
                this.consumer.endTable();
                return;
            }
        }
        int lineNum = 0;
        for (String s : Files.readLines(aFile, Charset.forName(this.getEncoding()))) {
            if (lineNum == 0 && this.headerNames == null) {
                String[] header = headerSplitPattern.split(s);
                ITableMetaData tableMetaData = this.createMetaData(aFile, header);
                this.consumer.startTable(tableMetaData);
                if (!this.loadData) {
                    break;
                }
            } else {
                Object[] row = this.dataSplitPattern.split(s);
                this.consumer.row(row);
            }
            lineNum++;
        }
        this.consumer.endTable();
    }

    protected ITableMetaData createMetaData(File aFile, String[] header) {
        Column[] columns = new Column[header.length];

        for (int i = 0; i < header.length; i++) {
            String columnName = header[i];
            columnName = columnName.trim();
            columns[i] = new Column(columnName, DataType.UNKNOWN);
        }
        String tableName = aFile.getName().substring(0, aFile.getName().indexOf("."));
        return new DefaultTableMetaData(tableName, columns);
    }

    public String getEncoding() {
        return this.encoding;
    }

}
