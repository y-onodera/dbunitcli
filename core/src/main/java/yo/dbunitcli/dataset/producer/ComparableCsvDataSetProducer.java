package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.common.handlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTableMappingContext;
import yo.dbunitcli.dataset.ComparableTableMappingTask;

import java.io.*;
import java.lang.reflect.Field;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

public record ComparableCsvDataSetProducer(ComparableDataSetParam param) implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableCsvDataSetProducer.class);

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new CsvTableExecutor(source, this.param);
    }

    private static class CsvTableExecutor implements ComparableTableMappingTask {
        private final Source source;
        private final ComparableDataSetParam param;
        private Pipeline pipeline;

        CsvTableExecutor(final Source source, final ComparableDataSetParam param) {
            this.source = source;
            this.param = param;
            this.resetThePipeline();
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            ComparableCsvDataSetProducer.LOGGER.info("produce - start filePath={}", this.source.filePath());
            try (final FileInputStream fi = new FileInputStream(this.source.filePath())) {
                final Reader reader = new BufferedReader(new InputStreamReader(fi, this.param.encoding()));
                final LineNumberReader lineNumberReader = new LineNumberReader(reader);

                this.skipToStartRow(lineNumberReader);
                String[] headerName = this.param.headerNames();
                if (headerName == null) {
                    headerName = this.parseFirstLine(lineNumberReader, this.source.filePath());
                }
                final TableMetaDataWithSource metaData = this.source.createMetaData(headerName);
                context.startTable(metaData);
                if (this.param.loadData()) {
                    int rows = 0;
                    List<Object> columns;
                    while ((columns = this.collectExpectedNumberOfColumns(headerName.length, lineNumberReader)) != null) {
                        context.row(metaData.source().apply(columns));
                        rows++;
                    }
                    ComparableCsvDataSetProducer.LOGGER.info("produce - rows={}", rows);
                }
                context.endTable();
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
            ComparableCsvDataSetProducer.LOGGER.info("produce - end   filePath={}", this.source.filePath());
        }

        @Override
        public Source source() {
            return this.source;
        }

        private String[] parseFirstLine(final LineNumberReader lineNumberReader, final String source) {
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

        private List<Object> parse(final String csv) throws PipelineException, IllegalInputCharacterException {
            this.pipeline.resetProducts();
            final CharacterIterator iterator = new StringCharacterIterator(csv);
            for (char c = iterator.first(); c != '\uffff'; c = iterator.next()) {
                this.pipeline.handle(c);
            }
            this.pipeline.noMoreInput();
            this.pipeline.thePieceIsDone();
            return this.pipeline.getProducts();
        }

        private List<Object> collectExpectedNumberOfColumns(final int expectedNumberOfColumns, final LineNumberReader lineNumberReader) {
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

        private void skipToStartRow(final LineNumberReader reader) throws IOException {
            for (int i = 1; i < this.param.startRow(); i++) {
                final String line = reader.readLine();
                if (line == null) {
                    throw new IllegalStateException(
                            String.format("File has fewer lines than startRow. Required: %d, Actual: %d",
                                    this.param.startRow(), i));
                }
            }
        }

        private void resetThePipeline() {
            this.pipeline = new Pipeline() {
                @Override
                public void putFront(final PipelineComponent component) {
                    if (component instanceof WhitespacesHandler) {
                        super.putFront(IgnoreDelimiterWhitespacesHandler.GET(CsvTableExecutor.this.param.delimiter(), component));
                    } else if (component instanceof EnforceHandler) {
                        super.putFront(LightEnforceHandler.ENFORCE((EnforceHandler) component));
                    } else {
                        super.putFront(component);
                    }
                }
            };
            this.pipeline.getPipelineConfig().setSeparatorChar(this.param.delimiter());
            this.pipeline.putFront(SeparatorHandler.ENDPIECE());
            this.pipeline.putFront(EscapeHandler.ACCEPT());
            this.pipeline.putFront(IsAlnumHandler.QUOTE());
            if (!this.param.ignoreQuoted()) {
                this.pipeline.putFront(QuoteHandler.QUOTE());
            }
            this.pipeline.putFront(EscapeHandler.ESCAPE());
            this.pipeline.putFront(WhitespacesHandler.IGNORE());
            this.pipeline.putFront(TransparentHandler.IGNORE());
        }
    }

    private static class IgnoreDelimiterWhitespacesHandler extends AbstractPipelineComponent {

        static Field HANDLE;

        static {
            try {
                IgnoreDelimiterWhitespacesHandler.HANDLE = AbstractPipelineComponent.class.getDeclaredField("helper");
            } catch (final NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            IgnoreDelimiterWhitespacesHandler.HANDLE.setAccessible(true);
        }

        private final char delimiter;

        public static PipelineComponent IGNORE(final char delimiter) {
            ComparableCsvDataSetProducer.LOGGER.debug("IGNORE() - start");
            return AbstractPipelineComponent.createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new Ignore());
        }

        public static PipelineComponent ACCEPT(final char delimiter) {
            ComparableCsvDataSetProducer.LOGGER.debug("ACCEPT() - start");
            return AbstractPipelineComponent.createPipelineComponent(new IgnoreDelimiterWhitespacesHandler(delimiter), new Accept());
        }

        public static PipelineComponent GET(final char delimiter, final PipelineComponent component) {
            try {

                final Helper helper = (Helper) IgnoreDelimiterWhitespacesHandler.HANDLE.get(component);
                if (helper instanceof ACCEPT) {
                    return IgnoreDelimiterWhitespacesHandler.ACCEPT(delimiter);
                }
                return IgnoreDelimiterWhitespacesHandler.IGNORE(delimiter);
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        IgnoreDelimiterWhitespacesHandler(final char delimiter) {
            this.delimiter = delimiter;
        }

        @Override
        public boolean canHandle(final char c) throws IllegalInputCharacterException {
            if (ComparableCsvDataSetProducer.LOGGER.isDebugEnabled()) {
                ComparableCsvDataSetProducer.LOGGER.debug("canHandle(c={}) - start", c);
            }
            return c != this.delimiter && Character.isWhitespace(c);
        }

        static class Ignore extends IGNORE {

        }

        static class Accept extends ACCEPT {

        }
    }

    private static class LightEnforceHandler extends AbstractPipelineComponent {
        static Field HANDLE;

        static {
            try {
                LightEnforceHandler.HANDLE = EnforceHandler.class.getDeclaredField("enforcedComponents");
            } catch (final NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            LightEnforceHandler.HANDLE.setAccessible(true);
        }

        private PipelineComponent[] enforcedComponents;
        private PipelineComponent theHandlerComponent;

        public static PipelineComponent ENFORCE(final EnforceHandler components) {
            final LightEnforceHandler handler = new LightEnforceHandler(components);
            return AbstractPipelineComponent.createPipelineComponent(handler, new ENFORCE(handler));
        }

        private LightEnforceHandler(final EnforceHandler components) {
            try {
                this.setEnforcedComponents((PipelineComponent[]) LightEnforceHandler.HANDLE.get(components));
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
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
