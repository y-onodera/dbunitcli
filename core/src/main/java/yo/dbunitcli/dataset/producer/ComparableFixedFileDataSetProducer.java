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

public record ComparableFixedFileDataSetProducer(ComparableDataSetParam param,
                                                 List<Integer> columnLengths) implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableFixedFileDataSetProducer.class);

    public ComparableFixedFileDataSetProducer(final ComparableDataSetParam param) {
        this(param, Arrays.stream(param.fixedLength().split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public Runnable createExecuteTableTask(final Source source, final ComparableDataSetConsumer consumer) {
        return new FixedFileTableExecutor(source, consumer, this.param, this.columnLengths);
    }

    private record FixedFileTableExecutor(Source source, ComparableDataSetConsumer consumer,
                                          ComparableDataSetParam param,
                                          List<Integer> columnLengths) implements Runnable {

        @Override
        public void run() {
            try {
                ComparableFixedFileDataSetProducer.LOGGER.info("produce - start filePath={}", this.source.filePath());
                final TableMetaDataWithSource metaData = this.source.createMetaData(this.param.headerNames());
                this.consumer.startTable(metaData);
                if (this.param.loadData()) {
                    int rows = 1;
                    for (final String s : Files.readAllLines(Path.of(this.source.filePath()), Charset.forName(this.param.encoding()))) {
                        if (rows >= this.param.startRow()) {
                            this.consumer.row(metaData.source().apply(this.split(s)));
                        }
                        rows++;
                    }
                    ComparableFixedFileDataSetProducer.LOGGER.info("produce - rows={}", rows);
                }
                this.consumer.endTable();
                ComparableFixedFileDataSetProducer.LOGGER.info("produce - end   filePath={}", this.source.filePath());
            } catch (final IOException | DataSetException e) {
                throw new AssertionError(e);
            }
        }

        private Object[] split(final String s) throws UnsupportedEncodingException {
            final Object[] result = new Object[this.columnLengths.size()];
            final byte[] bytes = s.getBytes(this.param.encoding());
            int from = 0;
            int to = 0;
            for (int index = 0, max = this.columnLengths.size(); index < max; index++) {
                to = to + this.columnLengths.get(index);
                result[index] = new String(Arrays.copyOfRange(bytes, from, to), this.param.encoding());
                from = from + this.columnLengths.get(index);
            }
            return result;
        }
    }
}
