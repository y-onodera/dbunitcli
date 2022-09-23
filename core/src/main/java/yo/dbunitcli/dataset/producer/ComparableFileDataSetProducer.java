package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.dataset.TableNameFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.function.Predicate;

public class ComparableFileDataSetProducer implements ComparableDataSetProducer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private IDataSetConsumer consumer;
    private final File src;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;
    private int rows = 0;

    public ComparableFileDataSetProducer(ComparableDataSetParam param) {
        this.param = param;
        this.src = this.param.getSrc().getAbsoluteFile();
        this.filter = this.param.getTableNameFilter();
        this.loadData = this.param.isLoadData();
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
        ITableMetaData metaData = new ComparableFileTableMetaData(this.src.getName());
        this.consumer.startTable(metaData);
        LOGGER.info("produce - start fileName={}", this.src);
        if (this.loadData) {
            try {
                Files.walk(this.src.toPath())
                        .filter(fileTypeFilter())
                        .filter(path -> this.filter.predicate(path.toString()))
                        .forEach(path -> this.produceFromFile(path.toFile()));
            } catch (IOException | AssertionError e) {
                throw new DataSetException("error producing dataSet for '" + this.src + "'", e);
            }
        }
        this.consumer.endTable();
        LOGGER.info("produce - rows={}", this.rows);
        LOGGER.info("produce - end   fileName={}", this.src);
        this.consumer.endDataSet();
        LOGGER.info("produce() - end");
    }

    protected Predicate<Path> fileTypeFilter() {
        if (Strings.isNullOrEmpty(this.param.getExtension())) {
            return path -> path.toFile().isFile();
        }
        return path -> path.toFile().isFile()
                && path.toString().toUpperCase().endsWith(this.param.getExtension().toUpperCase());
    }

    protected void produceFromFile(File file) {
        Object[] row = new Object[6];
        row[0] = file.getAbsolutePath();
        row[1] = file.getName();
        row[2] = file.getParent();
        row[3] = this.src.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
        row[4] = file.length() / 1024;
        row[5] = DATE_FORMAT.format(file.lastModified());
        try {
            this.consumer.row(row);
            this.rows++;
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
