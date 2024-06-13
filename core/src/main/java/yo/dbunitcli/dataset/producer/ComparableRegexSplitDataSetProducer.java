package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class ComparableRegexSplitDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);
    private final File[] src;
    private final String encoding;
    private final Pattern dataSplitPattern;
    private final ComparableDataSetParam param;
    private IDataSetConsumer consumer;
    private String[] headerNames;
    private Pattern headerSplitPattern;

    public ComparableRegexSplitDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.encoding = this.param.encoding();
        final String headerName = this.param.headerName();
        if (!Optional.ofNullable(headerName).orElse("").isEmpty()) {
            this.headerNames = headerName.split(",");
        }
        if (this.headerNames == null) {
            this.headerSplitPattern = Pattern.compile(this.param.headerSplitPattern());
        }
        this.dataSplitPattern = Pattern.compile(this.param.dataSplitPattern());
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
        ComparableRegexSplitDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(it -> this.param.tableNameFilter().predicate(this.getTableName(it)))
                .forEach(this::produceFromFile);
        this.consumer.endDataSet();
        ComparableRegexSplitDataSetProducer.LOGGER.info("produce() - end");
    }

    protected void produceFromFile(final File aFile) {
        try {
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - start fileName={}", aFile);
            if (this.headerNames != null) {
                this.consumer.startTable(this.createMetaData(aFile, this.headerNames));
                if (!this.param.loadData()) {
                    this.consumer.endTable();
                    return;
                }
            }
            int lineNum = 0;
            for (final String s : Files.readAllLines(aFile.toPath(), Charset.forName(this.getEncoding()))) {
                if (lineNum == 0 && this.headerNames == null) {
                    this.consumer.startTable(this.createMetaData(aFile, this.headerSplitPattern.split(s)));
                    if (!this.getParam().loadData()) {
                        break;
                    }
                } else {
                    this.consumer.row(this.dataSplitPattern.split(s));
                }
                lineNum++;
            }
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - rows={}", lineNum);
            this.consumer.endTable();
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - end   fileName={}", aFile);
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private String getEncoding() {
        return this.encoding;
    }

}
