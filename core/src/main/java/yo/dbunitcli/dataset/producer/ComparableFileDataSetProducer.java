package yo.dbunitcli.dataset.producer;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Predicate;

public class ComparableFileDataSetProducer implements ComparableDataSetProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableFileDataSetProducer.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final File src;
    private final ComparableDataSetParam param;
    private final boolean loadData;
    private IDataSetConsumer consumer;
    private int rows = 0;

    public ComparableFileDataSetProducer(final ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.src().getAbsoluteFile();
        this.loadData = this.param.loadData();
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
        ComparableFileDataSetProducer.LOGGER.info("produce() - start");
        this.consumer.startDataSet();
        final ITableMetaData metaData = new ComparableFileTableMetaData(this.src.getName());
        this.consumer.startTable(metaData);
        ComparableFileDataSetProducer.LOGGER.info("produce - start fileName={}", this.src);
        if (this.loadData) {
            try {
                this.param.getWalk()
                        .filter(this.fileTypeFilter())
                        .forEach(path -> this.produceFromFile(path.toFile()));
            } catch (final AssertionError e) {
                throw new DataSetException("error producing dataSet for '" + this.src + "'", e);
            }
        }
        this.consumer.endTable();
        ComparableFileDataSetProducer.LOGGER.info("produce - rows={}", this.rows);
        ComparableFileDataSetProducer.LOGGER.info("produce - end   fileName={}", this.src);
        this.consumer.endDataSet();
        ComparableFileDataSetProducer.LOGGER.info("produce() - end");
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
        row[0] = file.toPath().toAbsolutePath().normalize().toString();
        row[1] = file.getName();
        row[2] = file.getParent();
        row[3] = this.src.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
        row[4] = file.length() / 1024;
        row[5] = ComparableFileDataSetProducer.DATE_FORMAT.format(file.lastModified());
        try {
            this.consumer.row(row);
            this.rows++;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
