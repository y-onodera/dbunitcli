package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ComparableFixedFileDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableFixedFileDataSetProducer.class);
    private final ComparableDataSetParam param;
    private final File[] src;
    private final int startRow;
    private final String[] headerNames;
    private final List<Integer> columnLengths;
    private final boolean loadData;
    private final boolean addFileInfo;
    private ComparableDataSetConsumer consumer;

    public ComparableFixedFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.startRow = this.param.startRow();
        this.headerNames = this.param.headerName().split(",");
        this.loadData = this.param.loadData();
        this.addFileInfo = this.param.addFileInfo();
        this.columnLengths = Arrays.stream(this.param.fixedLength().split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableFixedFileDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(it -> this.getParam().tableNameFilter().predicate(this.getTableName(it)))
                .forEach(it -> this.executeTable(this.getSource(it, this.addFileInfo)));
        this.consumer.endDataSet();
        ComparableFixedFileDataSetProducer.LOGGER.info("produce() - end");
    }

    @Override
    public void executeTable(final Source source) {
        try {
            ComparableFixedFileDataSetProducer.LOGGER.info("produce - start filePath={}", source.filePath());
            final TableMetaDataWithSource metaData = this.createMetaData(source, this.headerNames);
            this.consumer.startTable(metaData);
            if (this.loadData) {
                int rows = 1;
                for (final String s : Files.readAllLines(Path.of(source.filePath()), Charset.forName(this.getEncoding()))) {
                    if (rows >= this.startRow) {
                        this.consumer.row(metaData.source().apply(this.split(s)));
                    }
                    rows++;
                }
                ComparableFixedFileDataSetProducer.LOGGER.info("produce - rows={}", rows);
            }
            this.consumer.endTable();
            ComparableFixedFileDataSetProducer.LOGGER.info("produce - end   filePath={}", source.filePath());
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
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
