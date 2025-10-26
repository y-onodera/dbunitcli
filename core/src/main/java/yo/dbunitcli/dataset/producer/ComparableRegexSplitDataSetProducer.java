package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.common.TableMetaDataWithSource;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTableMappingContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public record ComparableRegexSplitDataSetProducer(ComparableDataSetParam param
        , Pattern dataSplitPattern
        , Pattern headerSplitPattern) implements ComparableDataSetProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableRegexSplitDataSetProducer.class);

    public ComparableRegexSplitDataSetProducer(final ComparableDataSetParam param) {
        this(param
                , Pattern.compile(param.dataSplitPattern())
                , param.headerNames() != null ? null : Pattern.compile(param.headerSplitPattern())
        );
    }

    @Override
    public Runnable createTableMappingTask(final Source source, final ComparableTableMappingContext context) {
        return new RegexSplitTableExecutor(source, context, this.param,
                this.headerSplitPattern, this.dataSplitPattern);
    }

    private record RegexSplitTableExecutor(Source source, ComparableTableMappingContext context,
                                           ComparableDataSetParam param, Pattern headerSplitPattern,
                                           Pattern dataSplitPattern) implements Runnable {

        @Override
        public void run() {
            try {
                ComparableRegexSplitDataSetProducer.LOGGER.info("produce - start filePath={}", this.source.filePath());
                if (this.param.headerNames() != null) {
                    this.context.startTable(this.source.createMetaData(this.param.headerNames()));
                    if (!this.param.loadData()) {
                        this.context.endTable();
                        return;
                    }
                }
                int row = 1;
                for (final String s : Files.readAllLines(Path.of(this.source.filePath()), Charset.forName(this.param.encoding()))) {
                    if (row == this.param.startRow() && this.param.headerNames() == null) {
                        final TableMetaDataWithSource metaData = this.source.createMetaData(this.headerSplitPattern.split(s));
                        this.context.startTable(metaData);
                        if (!this.param.loadData()) {
                            break;
                        }
                    } else if (row >= this.param.startRow()) {
                        this.context.row(this.source.apply(this.dataSplitPattern.split(s)));
                    }
                    row++;
                }
                ComparableRegexSplitDataSetProducer.LOGGER.info("produce - rows={}", row);
                this.context.endTable();
                ComparableRegexSplitDataSetProducer.LOGGER.info("produce - end   filePath={}", this.source.filePath());
            } catch (final IOException e) {
                throw new AssertionError(e);
            }
        }
    }

}
