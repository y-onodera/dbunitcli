package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public record ComparableRegexSplitDataSetProducer(ComparableDataSetParam param) implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new RegexSplitTableExecutor(source, this.param);
    }

    private record RegexSplitTableExecutor(Source source, ComparableDataSetParam param, Pattern dataSplitPattern,
                                           Pattern headerSplitPattern) implements ComparableTableMappingTask {

        public RegexSplitTableExecutor(final Source source, final ComparableDataSetParam param) {
            this(source, param
                    , Pattern.compile(param.dataSplitPattern())
                    , param.headerNames() != null ? null : Pattern.compile(param.headerSplitPattern())
            );
        }

        @Override
        public void run(final ComparableTableMappingContext context) {
            try {
                ComparableRegexSplitDataSetProducer.LOGGER.info("produce - start filePath={}", this.source.filePath());
                if (!this.param.loadData()) {
                    if (this.param.headerNames() != null) {
                        final ComparableTableMapper mapper = context.createMapper(this.source.createMetaData(this.param.headerNames()));
                        mapper.startTable();
                        mapper.endTable();
                        return;
                    }
                    int row = 1;
                    for (final String s : Files.readAllLines(Path.of(this.source.filePath()), Charset.forName(this.param.encoding()))) {
                        if (row == this.param.startRow() && this.param.headerNames() == null) {
                            final TableMetaDataWithSource metaData = this.source.createMetaData(this.headerSplitPattern.split(s));
                            final ComparableTableMapper mapper = context.createMapper(metaData);
                            mapper.startTable();
                            mapper.endTable();
                            return;
                        }
                        row++;
                    }
                }
                int row = 1;
                final ComparableTableMapper mapper;
                try (final BufferedReader reader = Files.newBufferedReader(Path.of(this.source.filePath()), Charset.forName(this.param.encoding()))) {
                    if (this.param.headerNames() != null) {
                        mapper = context.createMapper(this.source.createMetaData(this.param.headerNames()));
                    } else {
                        int readed = 1;
                        String header = "";
                        for (; ; ) {
                            header = reader.readLine();
                            if (readed == this.param.startRow()) {
                                break;
                            }
                            readed++;
                        }
                        mapper = context.createMapper(this.source.createMetaData(this.headerSplitPattern.split(header)));
                    }
                    mapper.startTable();
                    for (; ; ) {
                        final String s = reader.readLine();
                        if (s == null) {
                            break;
                        }
                        mapper.addRow(this.source.apply(this.dataSplitPattern.split(s)));
                        row++;
                    }
                }
                if (this.param.loadData()) {
                    ComparableRegexSplitDataSetProducer.LOGGER.info("produce - rows={}", row);
                }
                mapper.endTable();
                ComparableRegexSplitDataSetProducer.LOGGER.info("produce - end   filePath={}", this.source.filePath());
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new RegexSplitTableExecutor(this.source, builder.build());
        }
    }

}
