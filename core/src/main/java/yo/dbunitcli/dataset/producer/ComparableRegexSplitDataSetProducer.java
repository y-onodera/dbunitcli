package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ComparableRegexSplitDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);
    private final File[] src;
    private final String encoding;
    private final Pattern dataSplitPattern;
    private final ComparableDataSetParam param;
    private final int startRow;
    private final String[] headerNames;
    private final boolean loadData;
    private final boolean addFileInfo;
    private Pattern headerSplitPattern;
    private ComparableDataSetConsumer consumer;

    public ComparableRegexSplitDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.encoding = this.param.encoding();
        this.startRow = this.param.startRow();
        this.headerNames = this.param.headerNames();
        if (this.headerNames == null) {
            this.headerSplitPattern = Pattern.compile(this.param.headerSplitPattern());
        }
        this.dataSplitPattern = Pattern.compile(this.param.dataSplitPattern());
        this.loadData = this.param.loadData();
        this.addFileInfo = this.param.addFileInfo();
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer iDataSetConsumer) {
        this.consumer = iDataSetConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        ComparableRegexSplitDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        Arrays.stream(this.src)
                .filter(it -> this.param.tableNameFilter().predicate(this.getTableName(it)))
                .forEach(it -> this.executeTable(this.getSource(it, this.addFileInfo)));
        this.consumer.endDataSet();
        ComparableRegexSplitDataSetProducer.LOGGER.info("produce() - end");
    }

    public void executeTable(final Source fileInfo) {
        try {
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - start filePath={}", fileInfo.filePath());
            if (this.headerNames != null) {
                this.consumer.startTable(this.createMetaData(fileInfo, this.headerNames));
                if (!this.loadData) {
                    this.consumer.endTable();
                    return;
                }
            }
            int row = 1;
            for (final String s : Files.readAllLines(Path.of(fileInfo.filePath()), Charset.forName(this.getEncoding()))) {
                if (row == this.startRow && this.headerNames == null) {
                    final TableMetaDataWithSource metaData = this.createMetaData(fileInfo, this.headerSplitPattern.split(s));
                    this.consumer.startTable(metaData);
                    if (!this.loadData) {
                        break;
                    }
                } else if (row >= this.startRow) {
                    this.consumer.row(fileInfo.apply(this.dataSplitPattern.split(s)));
                }
                row++;
            }
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - rows={}", row);
            this.consumer.endTable();
            ComparableRegexSplitDataSetProducer.LOGGER.info("produce - end   filePath={}", fileInfo.filePath());
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    private String getEncoding() {
        return this.encoding;
    }

}
