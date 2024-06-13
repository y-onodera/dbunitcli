package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableFixedFileDataSetProducer.class);
    private final File[] src;
    private final String[] headerNames;
    private final List<Integer> columnLengths;
    private final ComparableDataSetParam param;
    private IDataSetConsumer consumer;

    public ComparableFixedFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.headerNames = this.param.headerName().split(",");
        this.columnLengths = Arrays.stream(this.param.fixedLength().split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
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
        ComparableFixedFileDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(it -> this.getParam().tableNameFilter().predicate(this.getTableName(it)))
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        ComparableFixedFileDataSetProducer.LOGGER.info("produce() - end");
    }

    protected void produceFromFile(final File aFile) {
        try {
            ComparableFixedFileDataSetProducer.LOGGER.info("produce - start fileName={}", aFile);
            this.consumer.startTable(this.createMetaData(aFile, this.headerNames));
            if (this.param.loadData()) {
                int rows = 0;
                for (final String s : Files.readAllLines(aFile.toPath(), Charset.forName(this.getEncoding()))) {
                    this.consumer.row(this.split(s));
                    rows++;
                }
                ComparableFixedFileDataSetProducer.LOGGER.info("produce - rows={}", rows);
            }
            this.consumer.endTable();
            ComparableFixedFileDataSetProducer.LOGGER.info("produce - end   fileName={}", aFile);
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected Object[] split(final String s) throws UnsupportedEncodingException {
        final Object[] result = new Object[this.columnLengths.size()];
        final byte[] bytes = s.getBytes(this.getEncoding());
        int from = 0;
        int to = 0;
        for (int index = 0, max = this.columnLengths.size(); index < max; index++) {
            to = to + this.columnLengths.get(index);
            result[index] = new String(Arrays.copyOfRange(bytes, from, to), this.getEncoding());
            from = from + this.columnLengths.get(index);
        }
        return result;
    }

    protected String getEncoding() {
        return this.param.encoding();
    }
}
