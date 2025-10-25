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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ComparableRegexSplitDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);
    private final Pattern dataSplitPattern;
    private final ComparableDataSetParam param;
    private final int startRow;
    private Pattern headerSplitPattern;
    private ComparableDataSetConsumer consumer;

    public ComparableRegexSplitDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.startRow = this.param.startRow();
        if (this.getHeaderNames() == null) {
            this.headerSplitPattern = Pattern.compile(this.param.headerSplitPattern());
        }
        this.dataSplitPattern = Pattern.compile(this.param.dataSplitPattern());
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
    public void executeTable(final Source fileInfo) {
        try {
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - start filePath={}", fileInfo.filePath());
            if (this.getHeaderNames() != null) {
                this.getConsumer().startTable(this.createMetaData(fileInfo, this.getHeaderNames()));
                if (!this.loadData()) {
                    this.getConsumer().endTable();
                    return;
                }
            }
            int row = 1;
            for (final String s : Files.readAllLines(Path.of(fileInfo.filePath()), Charset.forName(this.getEncoding()))) {
                if (row == this.startRow && this.getHeaderNames() == null) {
                    final TableMetaDataWithSource metaData = this.createMetaData(fileInfo, this.headerSplitPattern.split(s));
                    this.getConsumer().startTable(metaData);
                    if (!this.loadData()) {
                        break;
                    }
                } else if (row >= this.startRow) {
                    this.getConsumer().row(fileInfo.apply(this.dataSplitPattern.split(s)));
                }
                row++;
            }
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - rows={}", row);
            this.getConsumer().endTable();
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - end   filePath={}", fileInfo.filePath());
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

}
