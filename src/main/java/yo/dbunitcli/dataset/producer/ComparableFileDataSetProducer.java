package yo.dbunitcli.dataset.producer;

import com.google.common.base.Strings;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ComparableFileDataSetProducer.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private IDataSetConsumer consumer = new DefaultConsumer();
    private final File src;
    private final TableNameFilter filter;
    private final ComparableDataSetParam param;
    private final boolean loadData;

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
        logger.info("produce() - start");
        this.consumer.startDataSet();
        ITableMetaData metaData = new ComparableFileTableMetaData(this.src.getName());
        this.consumer.startTable(metaData);
        if (this.loadData) {
            try {
                Files.walk(this.src.toPath())
                        .filter(fileTypeFilter())
                        .filter(path -> this.filter.predicate(path.toString()))
                        .forEach(path -> {
                            this.produceFromFile(path.toFile());
                        });
            } catch (IOException | AssertionError e) {
                throw new DataSetException("error producing dataSet for '" + this.src.toString() + "'", e);
            }
        }
        this.consumer.endTable();
        this.consumer.endDataSet();
    }

    protected Predicate<Path> fileTypeFilter() {
        if (Strings.isNullOrEmpty(this.param.getExtension())) {
            return path -> path.toFile().isFile();
        }
        return path -> path.toFile().isFile() && path.endsWith(this.param.getExtension());
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
        } catch (DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
