package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.common.handlers.IllegalInputCharacterException;
import org.dbunit.dataset.common.handlers.PipelineException;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.File;
import java.io.IOException;

public class ComparableCsvDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LogManager.getLogger();
    private IDataSetConsumer consumer;
    private final File[] src;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    final String encoding;
    final String[] headerNames;
    final boolean loadData;
    private final char delimiter;
    private int processRow;

    public ComparableCsvDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.encoding = this.param.getEncoding();
        this.filter = this.param.getTableNameFilter();
        this.loadData = this.param.isLoadData();
        String headerName = this.param.getHeaderName();
        if (!Strings.isNullOrEmpty(headerName)) {
            this.headerNames = headerName.split(",");
        } else {
            this.headerNames = null;
        }
        this.delimiter = param.getDelimiter();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public void produce() throws DataSetException {
        LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        for (File file : this.src) {
            if (this.filter.predicate(file.getAbsolutePath()) && file.length() > 0) {
                try {
                    this.produceFromFile(file);
                } catch (CsvParserException | DataSetException e) {
                    throw new DataSetException("error producing dataSet for table '" + file + "'", e);
                }
            }
        }
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    protected void produceFromFile(File theDataFile) throws DataSetException, CsvParserException {
        LOGGER.info("produce - start fileName={}", theDataFile);
        try {
            new CsvProducer(this.delimiter).parse(this, theDataFile);
        } catch (PipelineException | IOException | IllegalInputCharacterException e) {
            throw new DataSetException(e);
        }
        LOGGER.info("produce - rows={}", this.processRow);
        LOGGER.info("produce - end   fileName={}", theDataFile);
    }

    protected void startTable(File file, String[] parseFirstLine) throws DataSetException {
        this.consumer.startTable(this.createMetaData(file, parseFirstLine));
        this.processRow = 0;
    }

    protected void add(Object[] values) throws DataSetException {
        this.consumer.row(values);
        this.processRow++;
    }

    protected void endTable() throws DataSetException {
        this.consumer.endTable();
    }
}
