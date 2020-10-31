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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ComparableFixedFileDataSetProducer implements ComparableDataSetProducer {
    private static final Logger logger = LoggerFactory.getLogger(ComparableFixedFileDataSetProducer.class);
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File[] src;
    private final String encoding;
    private String[] headerNames;
    private List<Integer> columnLengths;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;

    public ComparableFixedFileDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.encoding = this.param.getEncoding();
        this.headerNames = this.param.getHeaderName().split(",");
        this.columnLengths = Arrays.stream(this.param.getFixedLength().split(","))
                .map(it -> Integer.valueOf(it))
                .collect(Collectors.toCollection(ArrayList::new));
        this.filter = this.param.getTableNameFilter();
        this.loadData = this.param.isLoadData();
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
        ITableMetaData tableMetaData = this.createMetaData(aFile, this.headerNames);
        this.consumer.startTable(tableMetaData);
        if (!this.loadData) {
            this.consumer.endTable();
            return;
        }
        for (String s : Files.readLines(aFile, Charset.forName(this.getEncoding()))) {
            Object[] row = this.split(s);
            this.consumer.row(row);
        }
        this.consumer.endTable();
    }

    protected Object[] split(String s) throws UnsupportedEncodingException {
        Object[] result = new Object[this.columnLengths.size()];
        byte[] bytes = s.getBytes(this.encoding);
        int from = 0;
        int to = 0;
        for (int index = 0, max = this.columnLengths.size(); index < max; index++) {
            to = to + columnLengths.get(index);
            result[index] = new String(Arrays.copyOfRange(bytes, from, to), this.encoding);
            from = from + columnLengths.get(index);
        }
        return result;
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
