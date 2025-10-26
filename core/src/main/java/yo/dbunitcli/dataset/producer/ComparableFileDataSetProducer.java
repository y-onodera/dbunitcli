package yo.dbunitcli.dataset.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.ComparableTableMappingContext;

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
    public Runnable createTableMappingTask(final Source source, final ComparableTableMappingContext context) {
        return new FileTableExecutor(source, context, this.param, this.src, this.fileTypeFilter());
    }

    protected Predicate<Path> fileTypeFilter() {
        if (Optional.ofNullable(this.param.extension()).orElse("").isEmpty()) {
            return path -> path.toFile().isFile();
        }
        return path -> path.toFile().isFile()
                && path.toString().toUpperCase().endsWith(this.param.extension().toUpperCase());
    }

    private static class FileTableExecutor implements Runnable {
        private final Source source;
        private final ComparableTableMappingContext context;
        private final ComparableDataSetParam param;
        private final File src;
        private final Predicate<Path> fileTypeFilter;
        private int rows = 0;

        FileTableExecutor(final Source source, final ComparableTableMappingContext context,
                          final ComparableDataSetParam param, final File src,
                          final Predicate<Path> fileTypeFilter) {
            this.source = source;
            this.context = context;
            this.param = param;
            this.src = src;
            this.fileTypeFilter = fileTypeFilter;
        }

        @Override
        public void run() {
            this.context.startTable(this.source
                    .wrap(new ComparableFileTableMetaData(this.src.getName())));
            ComparableFileDataSetProducer.LOGGER.info("produce - start fileName={}", this.src);
            if (this.param.loadData()) {
                try {
                    this.param.getWalk()
                            .filter(this.fileTypeFilter)
                            .forEach(path -> this.produceFromFile(path.toFile()));
                } catch (final AssertionError e) {
                    throw new AssertionError("error producing dataSet for '" + this.src + "'", e);
                }
            }
            this.context.endTable();
            ComparableFileDataSetProducer.LOGGER.info("produce - rows={}", this.rows);
            ComparableFileDataSetProducer.LOGGER.info("produce - end   fileName={}", this.src);
        }

        private void produceFromFile(final File file) {
            final Object[] row = new Object[6];
            final Path normalize = file.toPath().toAbsolutePath().normalize();
            row[0] = normalize.toString();
            row[1] = file.getName();
            row[2] = normalize.getParent().toString();
            row[3] = this.src.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
            row[4] = file.length() / 1024;
            row[5] = ComparableFileDataSetProducer.DATE_FORMAT.format(file.lastModified());
            this.context.row(row);
            this.rows++;
        }
    }
}
