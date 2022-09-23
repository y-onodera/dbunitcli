package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.common.handlers.*;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.csv.CsvParserImpl;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

public class ComparableCsvDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCsvDataSetProducer.class);
    private final String extension;
    private IDataSetConsumer consumer;
    private final File[] src;
    private final String encoding;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;
    private String[] headerNames;
    private final char delimiter;

    public ComparableCsvDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.encoding = this.param.getEncoding();
        this.filter = this.param.getTableNameFilter();
        this.loadData = this.param.isLoadData();
        String headerName = this.param.getHeaderName();
        if (!Strings.isNullOrEmpty(headerName)) {
            this.headerNames = headerName.split(",");
        }
        this.delimiter = param.getDelimiter();
        this.extension = param.getExtension();
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

    private void produceFromFile(File theDataFile) throws DataSetException, CsvParserException {
        LOGGER.info("produce - start fileName={}", theDataFile);
        try {
            List<List<Object>> readData = new CsvParser().setDelimiter(this.delimiter).parse(
                    new BufferedReader(new InputStreamReader(new FileInputStream(theDataFile), this.encoding))
                    , theDataFile.toString());
            ITableMetaData metaData;
            int startRow = 1;
            if (this.headerNames == null) {
                metaData = this.createMetaData(theDataFile, readData.get(0).toArray(new String[]{}));
            } else {
                metaData = this.createMetaData(theDataFile, this.headerNames);
                startRow = 0;
            }
            this.consumer.startTable(metaData);
            if (this.loadData) {
                for (int i = startRow; i < readData.size(); i++) {
                    this.consumer.row(this.loadData(readData, i));
                }
                LOGGER.info("produce - rows={}", readData.size() - startRow);
            }
            this.consumer.endTable();
        } catch (PipelineException | IOException | IllegalInputCharacterException | NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
        LOGGER.info("produce - end   fileName={}", theDataFile);
    }

    protected Object[] loadData(List<List<Object>> readData, int i) {
        Object[] row = readData.get(i).toArray();
        for (int col = 0; col < row.length; col++) {
            row[col] = row[col].equals(CsvDataSetWriter.NULL) ? null : row[col];
        }
        return row;
    }

    static class CsvParser extends CsvParserImpl {

        CsvParser setDelimiter(char delimiter) throws NoSuchFieldException, IllegalAccessException {
            Field f = CsvParserImpl.class.getDeclaredField("pipeline");
            f.setAccessible(true);
            Pipeline pipeline = new Pipeline() {
                @Override
                public void putFront(PipelineComponent component) {
                    if (component instanceof WhitespacesHandler) {
                        super.putFront(IgnoreDelimiterWhitespacesHandler.GET(delimiter, component));
                    } else {
                        super.putFront(component);
                    }
                }
            };
            pipeline.getPipelineConfig().setSeparatorChar(delimiter);
            pipeline.putFront(SeparatorHandler.ENDPIECE());
            pipeline.putFront(EscapeHandler.ACCEPT());
            pipeline.putFront(IsAlnumHandler.QUOTE());
            pipeline.putFront(QuoteHandler.QUOTE());
            pipeline.putFront(EscapeHandler.ESCAPE());
            pipeline.putFront(WhitespacesHandler.IGNORE());
            pipeline.putFront(TransparentHandler.IGNORE());
            f.set(this, pipeline);
            return this;
        }

    }

    private static class IgnoreDelimiterWhitespacesHandler extends AbstractPipelineComponent {
        private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreDelimiterWhitespacesHandler.class);
        static Field HANDLE;

        static {
            try {
                HANDLE = AbstractPipelineComponent.class.getDeclaredField("helper");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            HANDLE.setAccessible(true);
        }

        private final char delimiter;

        IgnoreDelimiterWhitespacesHandler(char delimiter) {
            this.delimiter = delimiter;
        }

        public static PipelineComponent IGNORE(char delimiter) {
            LOGGER.debug("IGNORE() - start");
            return createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new Ignore());
        }

        public static PipelineComponent ACCEPT(char delimiter) {
            LOGGER.debug("ACCEPT() - start");
            return createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new Accept());
        }

        public static PipelineComponent GET(char delimiter, PipelineComponent component) {
            try {

                Helper helper = (Helper) HANDLE.get(component);
                if (helper instanceof AbstractPipelineComponent.ACCEPT) {
                    return IgnoreDelimiterWhitespacesHandler.ACCEPT(delimiter);
                }
                return IgnoreDelimiterWhitespacesHandler.IGNORE(delimiter);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean canHandle(char c) throws IllegalInputCharacterException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("canHandle(c={}) - start", c);
            }
            return c != delimiter && Character.isWhitespace(c);
        }

        static class Ignore extends IGNORE {

        }

        static class Accept extends ACCEPT {

        }
    }

}
