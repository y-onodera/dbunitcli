package yo.dbunitcli.dataset.producer;

import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetConsumer;
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
    private static final Logger LOGGER = LogManager.getLogger();
    private IDataSetConsumer consumer;
    private final File[] src;
    private final String encoding;
    private final String[] headerNames;
    private final List<Integer> columnLengths;
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
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
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
        LOGGER.info("produce() - end");
    }

    protected void executeQuery(File aFile) throws DataSetException, IOException {
        LOGGER.info("produce - start fileName={}", aFile);
        this.consumer.startTable(this.createMetaData(aFile, this.headerNames));
        if (this.loadData) {
            int rows = 0;
            for (String s : Files.readLines(aFile, Charset.forName(this.getEncoding()))) {
                this.consumer.row(this.split(s));
                rows++;
            }
            LOGGER.info("produce - rows={}", rows);
        }
        this.consumer.endTable();
        LOGGER.info("produce - end   fileName={}", aFile);
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

    public String getEncoding() {
        return this.encoding;
    }
}
