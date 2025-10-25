package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

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
    private final int startRow;
    private final List<Integer> columnLengths;
    private ComparableDataSetConsumer consumer;

    public ComparableFixedFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.startRow = this.param.startRow();
        this.columnLengths = Arrays.stream(this.param.fixedLength().split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public ComparableDataSetConsumer getConsumer() {
        return this.consumer;
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void executeTable(final Source source) {
        try {
            ComparableFixedFileDataSetProducer.LOGGER.info("produce - start filePath={}", source.filePath());
            final TableMetaDataWithSource metaData = this.createMetaData(source, this.getHeaderNames());
            this.getConsumer().startTable(metaData);
            if (this.loadData()) {
                int rows = 1;
                for (final String s : Files.readAllLines(Path.of(source.filePath()), Charset.forName(this.getEncoding()))) {
                    if (rows >= this.startRow) {
                        this.getConsumer().row(metaData.source().apply(this.split(s)));
                    }
                    rows++;
                }
                ComparableFixedFileDataSetProducer.LOGGER.info("produce - rows={}", rows);
            }
            this.getConsumer().endTable();
            ComparableFixedFileDataSetProducer.LOGGER.info("produce - end   filePath={}", source.filePath());
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

}
