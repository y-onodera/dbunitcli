package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
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
import java.nio.charset.Charset;
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

    public ComparableRegexSplitDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        if (this.param.getSrc().isDirectory()) {
            this.src = this.param.getSrc().listFiles(File::isFile);
        } else {
            this.src = new File[]{this.param.getSrc()};
        }
        this.encoding = this.param.getEncoding();
        String headerName = this.param.getHeaderName();
        if (!Strings.isNullOrEmpty(headerName)) {
            this.headerNames = headerName.split(",");
        }
        if (this.headerNames == null) {
            this.headerSplitPattern = Pattern.compile(this.param.getHeaderSplitPattern());
        }
        this.dataSplitPattern = Pattern.compile(this.param.getDataSplitPattern());
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
        if (this.headerNames != null) {
            this.consumer.startTable(this.createMetaData(aFile, this.headerNames));
            if (!this.loadData) {
                this.consumer.endTable();
                return;
            }
        }
        int lineNum = 0;
        for (String s : Files.readLines(aFile, Charset.forName(this.getEncoding()))) {
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
    }

    public String getEncoding() {
        return this.encoding;
    }

}
