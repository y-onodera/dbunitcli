package yo.dbunitcli.dataset.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.common.handlers.*;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.*;
import java.lang.reflect.Field;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ComparableCsvDataSetProducer implements ComparableDataSetProducer {
    private static final Logger LOGGER = LogManager.getLogger();
    private IDataSetConsumer consumer;
    private final File[] src;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final String encoding;
    private final String[] headerNames;
    private final boolean loadData;
    private final char delimiter;
    private int processRow;
    private Pipeline pipeline;

    public ComparableCsvDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrcFiles();
        this.encoding = this.param.encoding();
        this.filter = this.param.tableNameFilter();
        this.loadData = this.param.loadData();
        final String headerName = this.param.headerName();
        if (!Optional.ofNullable(headerName).orElse("").isEmpty()) {
            this.headerNames = headerName.split(",");
        } else {
            this.headerNames = null;
        }
        this.delimiter = param.delimiter();
        this.resetThePipeline();
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public void setConsumer(final IDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
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

    protected void produceFromFile(final File theDataFile) {
        LOGGER.info("produce - start fileName={}", theDataFile);
        this.parse(theDataFile);
        LOGGER.info("produce - rows={}", this.processRow);
        LOGGER.info("produce - end   fileName={}", theDataFile);
    }

    protected void parse(final File file) {
        try (final FileInputStream fi = new FileInputStream(file)) {
            final Reader reader = new BufferedReader(new InputStreamReader(fi, this.encoding));
            final LineNumberReader lineNumberReader = new LineNumberReader(reader);
            String[] headerName = this.headerNames;
            if (headerName == null) {
                headerName = this.parseFirstLine(lineNumberReader, file.getAbsolutePath());
            }
            this.consumer.startTable(this.createMetaData(file, headerName));
            this.processRow = 0;
            if (this.loadData) {
                this.parseTheData(headerName, lineNumberReader);
            }
            this.consumer.endTable();
        } catch (final IOException | DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected String[] parseFirstLine(final LineNumberReader lineNumberReader, final String source) {
        try {
            final String firstLine = lineNumberReader.readLine();
            if (firstLine == null) {
                throw new AssertionError("The first line of " + source + " is null");
            } else {
                return this.parse(firstLine).stream().map(Object::toString).toArray(String[]::new);
            }
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    protected List<Object> parse(final String csv) throws PipelineException, IllegalInputCharacterException {
        this.pipeline.resetProducts();
        final CharacterIterator iterator = new StringCharacterIterator(csv);
        for (char c = iterator.first(); c != '\uffff'; c = iterator.next()) {
            this.pipeline.handle(c);
        }
        this.pipeline.noMoreInput();
        this.pipeline.thePieceIsDone();
        return this.pipeline.getProducts();
    }

    protected void parseTheData(final String[] columnsInFirstLine, final LineNumberReader lineNumberReader) {
        try {
            List<Object> columns;
            while ((columns = this.collectExpectedNumberOfColumns(columnsInFirstLine.length, lineNumberReader)) != null) {
                this.consumer.row(columns.toArray(new Object[]{}));
                this.processRow++;
            }
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected List<Object> collectExpectedNumberOfColumns(final int expectedNumberOfColumns, final LineNumberReader lineNumberReader) {
        try {
            List<Object> columns = null;
            int columnsCollectedSoFar = 0;
            final StringBuilder buffer = new StringBuilder();
            String anotherLine = lineNumberReader.readLine();
            if (anotherLine == null) {
                return null;
            } else {
                boolean shouldProceed = false;
                while (columnsCollectedSoFar < expectedNumberOfColumns) {
                    try {
                        columns = this.parse(buffer.append(anotherLine.replaceAll("(?!<\\\\)\\\\(?![\\\\\"])", "\\\\\\\\")).toString());
                        columnsCollectedSoFar = columns.size();
                    } catch (final IllegalStateException var9) {
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
                    final String message = "Expected " + expectedNumberOfColumns + " columns on line " + lineNumberReader.getLineNumber() + ", got " + columnsCollectedSoFar + ". Offending line: " + buffer;
                    throw new AssertionError(message);
                } else {
                    return columns;
                }
            }
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

    protected void resetThePipeline() {
        this.pipeline = new Pipeline() {
            @Override
            public void putFront(final PipelineComponent component) {
                if (component instanceof WhitespacesHandler) {
                    super.putFront(IgnoreDelimiterWhitespacesHandler.GET(ComparableCsvDataSetProducer.this.delimiter, component));
                } else if (component instanceof EnforceHandler) {
                    super.putFront(LightEnforceHandler.ENFORCE((EnforceHandler) component));
                } else {
                    super.putFront(component);
                }
            }
        };
        this.pipeline.getPipelineConfig().setSeparatorChar(this.delimiter);
        this.pipeline.putFront(SeparatorHandler.ENDPIECE());
        this.pipeline.putFront(EscapeHandler.ACCEPT());
        this.pipeline.putFront(IsAlnumHandler.QUOTE());
        this.pipeline.putFront(QuoteHandler.QUOTE());
        this.pipeline.putFront(EscapeHandler.ESCAPE());
        this.pipeline.putFront(WhitespacesHandler.IGNORE());
        this.pipeline.putFront(TransparentHandler.IGNORE());
    }

    protected static class IgnoreDelimiterWhitespacesHandler extends AbstractPipelineComponent {

        private static final Logger LOGGER = LogManager.getLogger();
        static Field HANDLE;

        static {
            try {
                HANDLE = AbstractPipelineComponent.class.getDeclaredField("helper");
            } catch (final NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            HANDLE.setAccessible(true);
        }

        private final char delimiter;

        IgnoreDelimiterWhitespacesHandler(final char delimiter) {
            this.delimiter = delimiter;
        }

        public static PipelineComponent IGNORE(final char delimiter) {
            LOGGER.debug("IGNORE() - start");
            return createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new IgnoreDelimiterWhitespacesHandler.Ignore());
        }

        public static PipelineComponent ACCEPT(final char delimiter) {
            LOGGER.debug("ACCEPT() - start");
            return createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new IgnoreDelimiterWhitespacesHandler.Accept());
        }

        public static PipelineComponent GET(final char delimiter, final PipelineComponent component) {
            try {

                final Helper helper = (Helper) HANDLE.get(component);
                if (helper instanceof AbstractPipelineComponent.ACCEPT) {
                    return IgnoreDelimiterWhitespacesHandler.ACCEPT(delimiter);
                }
                return IgnoreDelimiterWhitespacesHandler.IGNORE(delimiter);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean canHandle(final char c) throws IllegalInputCharacterException {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("canHandle(c={}) - start", c);
            }
            return c != this.delimiter && Character.isWhitespace(c);
        }

        static class Ignore extends IGNORE {

        }

        static class Accept extends ACCEPT {

        }
    }

    static class LightEnforceHandler extends AbstractPipelineComponent {
        static Field HANDLE;

        static {
            try {
                HANDLE = EnforceHandler.class.getDeclaredField("enforcedComponents");
            } catch (final NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            HANDLE.setAccessible(true);
        }

        private PipelineComponent[] enforcedComponents;
        private PipelineComponent theHandlerComponent;

        private LightEnforceHandler(final EnforceHandler components) {
            try {
                this.setEnforcedComponents((PipelineComponent[]) HANDLE.get(components));
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public static PipelineComponent ENFORCE(final EnforceHandler components) {
            final LightEnforceHandler handler = new LightEnforceHandler(components);
            return createPipelineComponent(handler, new ENFORCE(handler));
        }

        @Override
        public boolean canHandle(final char c) {
            for (int i = 0; i < this.getEnforcedComponents().length; i++) {
                if (this.getEnforcedComponents()[i].canHandle(c)) {
                    this.setTheHandlerComponent(this.getEnforcedComponents()[i]);
                    return true;
                }
            }
            this.getPipeline().removeFront();
            return false;
        }

        @Override
        public void setPipeline(final Pipeline pipeline) {
            for (int i = 0; i < this.getEnforcedComponents().length; i++) {
                this.getEnforcedComponents()[i].setPipeline(pipeline);
            }
            super.setPipeline(pipeline);
        }

        protected PipelineComponent[] getEnforcedComponents() {
            return this.enforcedComponents;
        }

        protected void setEnforcedComponents(final PipelineComponent[] enforcedComponents) {
            this.enforcedComponents = enforcedComponents;
        }

        PipelineComponent getTheHandlerComponent() {
            return this.theHandlerComponent;
        }

        void setTheHandlerComponent(final PipelineComponent theHandlerComponent) {
            this.theHandlerComponent = theHandlerComponent;
        }

        static private class ENFORCE extends ACCEPT {
            LightEnforceHandler handler;

            public ENFORCE(final LightEnforceHandler handler) {
                this.handler = handler;
            }

            @Override
            public void helpWith(final char c) {

                try {
                    this.handler.getTheHandlerComponent().handle(c);
                    this.handler.getPipeline().removeFront();
                } catch (final PipelineException e) {
                    throw new RuntimeException(e.getMessage());
                }
                // ignore the char
            }
        }
    }
}
