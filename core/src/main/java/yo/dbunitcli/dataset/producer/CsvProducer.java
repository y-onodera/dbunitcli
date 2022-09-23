package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.common.handlers.*;
import org.dbunit.dataset.csv.CsvParserException;

import java.io.*;
import java.lang.reflect.Field;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

class CsvProducer {

    private Pipeline pipeline;
    private final char delimiter;

    CsvProducer(char delimiter) {
        this.delimiter = delimiter;
        this.resetThePipeline();
    }

    void parse(ComparableCsvDataSetProducer consumer, File file) throws IOException, DataSetException {
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), consumer.encoding));
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        String[] headerName = consumer.headerNames;
        if (headerName == null) {
            headerName = this.parseFirstLine(lineNumberReader, file.getAbsolutePath());
        }
        consumer.startTable(file, headerName);
        if (consumer.loadData) {
            parseTheData(headerName, lineNumberReader, consumer);
        }
        consumer.endTable();
    }

    private String[] parseFirstLine(LineNumberReader lineNumberReader, String source) throws IOException, CsvParserException {
        String firstLine = lineNumberReader.readLine();
        if (firstLine == null) {
            throw new CsvParserException("The first line of " + source + " is null");
        } else {
            return this.parse(firstLine).stream().map(it -> it.toString()).toArray(String[]::new);
        }
    }

    private List<Object> parse(String csv) throws PipelineException, IllegalInputCharacterException {
        this.pipeline.resetProducts();
        CharacterIterator iterator = new StringCharacterIterator(csv);

        for (char c = iterator.first(); c != '\uffff'; c = iterator.next()) {
            this.pipeline.handle(c);
        }

        this.pipeline.noMoreInput();
        this.pipeline.thePieceIsDone();
        return this.pipeline.getProducts();
    }

    private void parseTheData(String[] columnsInFirstLine, LineNumberReader lineNumberReader, ComparableCsvDataSetProducer consumer) throws IOException, CsvParserException, DataSetException {
        List<Object> columns;
        while ((columns = this.collectExpectedNumberOfColumns(columnsInFirstLine.length, lineNumberReader)) != null) {
            consumer.add(columns.toArray(new Object[]{}));
        }

    }

    private List<Object> collectExpectedNumberOfColumns(int expectedNumberOfColumns, LineNumberReader lineNumberReader) throws IOException, CsvParserException {

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

    private void resetThePipeline() {
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

    private static class IgnoreDelimiterWhitespacesHandler extends AbstractPipelineComponent {
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
