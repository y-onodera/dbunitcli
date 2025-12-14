package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.*;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ComparableFileDataSetProducer implements ComparableDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableFileDataSetProducer.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final File src;
    private final ComparableDataSetParam param;

    public ComparableFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.src().getAbsoluteFile();
    }

    @Override
    public ComparableDataSetParam param() {
        return this.param;
    }

    @Override
    public Stream<Source> getSourceStream() {
        return Stream.of(this.getSource(this.src));
    }

    @Override
    public ComparableTableMappingTask createTableMappingTask(final Source source) {
        return new FileTableExecutor(source, this.param, this.src, this.fileTypeFilter());
    }

    protected Predicate<Path> fileTypeFilter() {
        if (Optional.ofNullable(this.param.extension()).orElse("").isEmpty()) {
            return path -> path.toFile().isFile();
        }
        return path -> path.toFile().isFile()
                && path.toString().toUpperCase().endsWith(this.param.extension().toUpperCase());
    }

    private record FileTableExecutor(Source source, ComparableDataSetParam param,
                                     File src, Predicate<Path> fileTypeFilter) implements ComparableTableMappingTask {

        @Override
        public void run(final ComparableTableMappingContext context) {
            final ComparableTableMapper mapper = context.createMapper(this.source
                    .wrap(new ComparableFileTableMetaData(this.getTableName())));
            mapper.startTable();
            ComparableFileDataSetProducer.LOGGER.info("produce - start fileName={}", this.src);
            if (this.param.loadData()) {
                final int[] rows = {0};
                try {
                    this.param.getWalk()
                            .filter(this.fileTypeFilter)
                            .peek(it -> rows[0]++)
                            .forEach(path -> mapper.addRow(this.produceFromFile(path.toFile())));
                } catch (final AssertionError e) {
                    throw new AssertionError("error producing dataSet for '" + this.src + "'", e);
                }
                ComparableFileDataSetProducer.LOGGER.info("produce - rows={}", rows[0]);
            }
            mapper.endTable();
            ComparableFileDataSetProducer.LOGGER.info("produce - end   fileName={}", this.src);
        }

        @Override
        public ComparableTableMappingTask with(final ComparableDataSetParam.Builder builder) {
            return new FileTableExecutor(this.source, builder.build(), this.src, this.fileTypeFilter);
        }

        private String getTableName() {
            return this.src.getName();
        }

        private Object[] produceFromFile(final File file) {
            final Object[] row = new Object[6];
            final Path normalize = file.toPath().toAbsolutePath().normalize();
            row[0] = normalize.toString();
            row[1] = file.getName();
            row[2] = normalize.getParent().toString();
            row[3] = this.src.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
            row[4] = file.length() / 1024;
            row[5] = ComparableFileDataSetProducer.DATE_FORMAT.format(file.lastModified());
            return row;
        }
    }
}
