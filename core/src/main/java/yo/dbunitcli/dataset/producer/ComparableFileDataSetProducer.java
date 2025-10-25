package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

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
    private ComparableDataSetConsumer consumer;
    private int rows = 0;

    public ComparableFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.src().getAbsoluteFile();
    }

    @Override
    public void setConsumer(final ComparableDataSetConsumer aConsumer) {
        this.consumer = aConsumer;
    }

    @Override
    public ComparableDataSetConsumer getConsumer() {
        return this.consumer;
    }

    @Override
    public ComparableDataSetParam getParam() {
        return this.param;
    }

    @Override
    public Stream<Source> getSourceStream() {
        return Stream.of(this.getSource(this.src));
    }

    @Override
    public void executeTable(final Source source) {
        this.getConsumer().startTable(source
                .wrap(new ComparableFileTableMetaData(this.src.getName())));
        ComparableFileDataSetProducer.LOGGER.info("produce - start fileName={}", this.src);
        if (this.loadData()) {
            try {
                this.param.getWalk()
                        .filter(this.fileTypeFilter())
                        .forEach(path -> this.produceFromFile(path.toFile()));
            } catch (final AssertionError e) {
                throw new AssertionError("error producing dataSet for '" + this.src + "'", e);
            }
        }
        try {
            this.getConsumer().endTable();
        } catch (final DataSetException e) {
            throw new AssertionError("error producing dataSet for '" + this.src + "'", e);
        }
        ComparableFileDataSetProducer.LOGGER.info("produce - rows={}", this.rows);
        ComparableFileDataSetProducer.LOGGER.info("produce - end   fileName={}", this.src);
    }

    protected Predicate<Path> fileTypeFilter() {
        if (Optional.ofNullable(this.param.extension()).orElse("").isEmpty()) {
            return path -> path.toFile().isFile();
        }
        return path -> path.toFile().isFile()
                && path.toString().toUpperCase().endsWith(this.param.extension().toUpperCase());
    }

    protected void produceFromFile(final File file) {
        final Object[] row = new Object[6];
        final Path normalize = file.toPath().toAbsolutePath().normalize();
        row[0] = normalize.toString();
        row[1] = file.getName();
        row[2] = normalize.getParent().toString();
        row[3] = this.src.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
        row[4] = file.length() / 1024;
        row[5] = ComparableFileDataSetProducer.DATE_FORMAT.format(file.lastModified());
        try {
            this.getConsumer().row(row);
            this.rows++;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
