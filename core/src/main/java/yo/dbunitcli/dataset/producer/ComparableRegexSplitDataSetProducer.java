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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public class ComparableRegexSplitDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LogManager.getLogger();
    private IDataSetConsumer consumer;
    private final File[] src;
    private final String encoding;
    private final Pattern dataSplitPattern;
    private String[] headerNames;
    private Pattern headerSplitPattern;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;

    public ComparableRegexSplitDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        if (this.param.src().isDirectory()) {
            this.src = this.param.src().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.src()};
        }
        this.encoding = this.param.encoding();
        final String headerName = this.param.headerName();
        if (!Optional.ofNullable(headerName).orElse("").isEmpty()) {
            this.headerNames = headerName.split(",");
        }
        if (this.headerNames == null) {
            this.headerSplitPattern = Pattern.compile(this.param.headerSplitPattern());
        }
        this.dataSplitPattern = Pattern.compile(this.param.dataSplitPattern());
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
            if (this.headerNames != null) {
                this.consumer.startTable(this.createMetaData(aFile, this.headerNames));
                if (!this.loadData) {
                    this.consumer.endTable();
                    return;
                }
            }
            int lineNum = 0;
            for (final String s : Files.readAllLines(aFile.toPath(), Charset.forName(this.getEncoding()))) {
                if (lineNum == 0 && this.headerNames == null) {
                    this.consumer.startTable(this.createMetaData(aFile, this.headerSplitPattern.split(s)));
                    if (!this.loadData) {
                        break;
                    }
                } else {
                    this.consumer.row(this.dataSplitPattern.split(s));
                }
                lineNum++;
            }
            LOGGER.info("produce - rows={}", lineNum);
            this.consumer.endTable();
            LOGGER.info("produce - end   fileName={}", aFile);
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public String getEncoding() {
        return this.encoding;
    }

}
