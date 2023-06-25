package yo.dbunitcli.dataset.producer;

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
import java.nio.file.Files;
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

    public ComparableFixedFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        if (this.param.src().isDirectory()) {
            this.src = this.param.src().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.src()};
        }
        this.encoding = this.param.encoding();
        this.headerNames = this.param.headerName().split(",");
        this.columnLengths = Arrays.stream(this.param.fixedLength().split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
        this.filter = this.param.tableNameFilter();
        this.loadData = this.param.loadData();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(final IDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(file -> this.filter.predicate(file.getAbsolutePath()) && file.length() > 0)
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    protected void produceFromFile(final File aFile) {
        try {
            LOGGER.info("produce - start fileName={}", aFile);
            this.consumer.startTable(this.createMetaData(aFile, this.headerNames));
            if (this.loadData) {
                int rows = 0;
                for (final String s : Files.readAllLines(aFile.toPath(), Charset.forName(this.getEncoding()))) {
                    this.consumer.row(this.split(s));
                    rows++;
                }
                LOGGER.info("produce - rows={}", rows);
            }
            this.consumer.endTable();
            LOGGER.info("produce - end   fileName={}", aFile);
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected Object[] split(final String s) throws UnsupportedEncodingException {
        final Object[] result = new Object[this.columnLengths.size()];
        final byte[] bytes = s.getBytes(this.encoding);
        int from = 0;
        int to = 0;
        for (int index = 0, max = this.columnLengths.size(); index < max; index++) {
            to = to + this.columnLengths.get(index);
            result[index] = new String(Arrays.copyOfRange(bytes, from, to), this.encoding);
            from = from + this.columnLengths.get(index);
        }
        return result;
    }

    public String getEncoding() {
        return this.encoding;
    }
}
