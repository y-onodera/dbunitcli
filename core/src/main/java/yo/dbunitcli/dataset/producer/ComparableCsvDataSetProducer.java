package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.common.handlers.*;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.*;
import java.lang.reflect.Field;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

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
    private Pipeline pipeline;

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
        this.resetThePipeline();
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
            this.parse(theDataFile);
        } catch (PipelineException | IllegalInputCharacterException e) {
            throw new DataSetException(e);
        }
        LOGGER.info("produce - rows={}", this.processRow);
        LOGGER.info("produce - end   fileName={}", theDataFile);
    }

    protected void parse(File file) throws DataSetException {
        try (FileInputStream fi = new FileInputStream(file)) {
            Reader reader = new BufferedReader(new InputStreamReader(fi, this.encoding));
            LineNumberReader lineNumberReader = new LineNumberReader(reader);
            String[] headerName = this.headerNames;
            if (headerName == null) {
                headerName = this.parseFirstLine(lineNumberReader, file.getAbsolutePath());
            }
            this.consumer.startTable(this.createMetaData(file, headerName));
            this.processRow = 0;
            if (this.loadData) {
                parseTheData(headerName, lineNumberReader);
            }
            this.consumer.endTable();
        } catch (IOException e) {
            throw new DataSetException(e);
        }
    }

    protected String[] parseFirstLine(LineNumberReader lineNumberReader, String source) throws IOException, CsvParserException {
        String firstLine = lineNumberReader.readLine();
        if (firstLine == null) {
            throw new CsvParserException("The first line of " + source + " is null");
        } else {
            return this.parse(firstLine).stream().map(Object::toString).toArray(String[]::new);
        }
    }

    protected List<Object> parse(String csv) throws PipelineException, IllegalInputCharacterException {
        this.pipeline.resetProducts();
        CharacterIterator iterator = new StringCharacterIterator(csv);

        for (char c = iterator.first(); c != '\uffff'; c = iterator.next()) {
            this.pipeline.handle(c);
        }

        this.pipeline.noMoreInput();
        this.pipeline.thePieceIsDone();
        return this.pipeline.getProducts();
    }

    protected void parseTheData(String[] columnsInFirstLine, LineNumberReader lineNumberReader) throws IOException, CsvParserException, DataSetException {
        List<Object> columns;
        while ((columns = this.collectExpectedNumberOfColumns(columnsInFirstLine.length, lineNumberReader)) != null) {
            this.consumer.row(columns.toArray(new Object[]{}));
            this.processRow++;
        }
    }

    protected List<Object> collectExpectedNumberOfColumns(int expectedNumberOfColumns, LineNumberReader lineNumberReader) throws IOException, CsvParserException {
        List<Object> columns = null;
        int columnsCollectedSoFar = 0;
        StringBuilder buffer = new StringBuilder();
        String anotherLine = lineNumberReader.readLine();
        if (anotherLine == null) {
            return null;
        } else {
            boolean shouldProceed = false;
            while (columnsCollectedSoFar < expectedNumberOfColumns) {
                try {
                    columns = this.parse(buffer.append(anotherLine).toString());
                    columnsCollectedSoFar = columns.size();
                } catch (IllegalStateException var9) {
                    this.resetThePipeline();
                    anotherLine = lineNumberReader.readLine();
                    if (anotherLine == null) {
                        break;
                    }
                    buffer.append("\n");
                    shouldProceed = true;
                }
                if (!shouldProceed) {
                    break;
                }
            }
            if (columnsCollectedSoFar != expectedNumberOfColumns) {
                String message = "Expected " + expectedNumberOfColumns + " columns on line " + lineNumberReader.getLineNumber() + ", got " + columnsCollectedSoFar + ". Offending line: " + buffer;
                throw new CsvParserException(message);
            } else {
                return columns;
            }
        }
    }

    protected void resetThePipeline() {
        this.pipeline = new Pipeline() {
            @Override
            public void putFront(PipelineComponent component) {
                if (component instanceof WhitespacesHandler) {
                    super.putFront(IgnoreDelimiterWhitespacesHandler.GET(delimiter, component));
                } else {
                    super.putFront(component);
                }
            }
        };
        pipeline.getPipelineConfig().setSeparatorChar(this.delimiter);
        pipeline.putFront(SeparatorHandler.ENDPIECE());
        pipeline.putFront(EscapeHandler.ACCEPT());
        pipeline.putFront(IsAlnumHandler.QUOTE());
        pipeline.putFront(QuoteHandler.QUOTE());
        pipeline.putFront(EscapeHandler.ESCAPE());
        pipeline.putFront(WhitespacesHandler.IGNORE());
        pipeline.putFront(TransparentHandler.IGNORE());
    }

    protected static class IgnoreDelimiterWhitespacesHandler extends AbstractPipelineComponent {
        private static final Logger LOGGER = LogManager.getLogger();
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
            return createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new IgnoreDelimiterWhitespacesHandler.Ignore());
        }

        public static PipelineComponent ACCEPT(char delimiter) {
            LOGGER.debug("ACCEPT() - start");
            return createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new IgnoreDelimiterWhitespacesHandler.Accept());
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
