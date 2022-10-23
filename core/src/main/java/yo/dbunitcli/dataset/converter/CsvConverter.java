package yo.dbunitcli.dataset.converter;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;

public class CsvConverter extends CsvDataSetWriter implements IDataSetConverter {

    private static final Logger LOGGER = LogManager.getLogger();

    private final boolean exportEmptyTable;

    private final File resultDir;

    private final String encoding;

    private ITableMetaData activeMetaData;

    private int writeRows;

    private File file;

    public CsvConverter(DataSetConsumerParam param) {
        super(param.getResultDir());
        this.resultDir = param.getResultDir();
        this.encoding = param.getOutputEncoding();
        this.exportEmptyTable = param.isExportEmptyTable();
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        String activeTableName = metaData.getTableName();

        try {
            this.activeMetaData = metaData;
            this.writeRows = 0;
            Field f = CsvDataSetWriter.class.getDeclaredField("_activeMetaData");
            f.setAccessible(true);
            f.set(this, metaData);
            final File directory = new File(this.getTheDirectory());
            this.file = new File(directory, activeTableName + ".csv");
            LOGGER.info("convert - start fileName={}", this.file);
            if (!directory.exists()) {
                Files.createDirectories(directory.toPath());
            }
            Files.deleteIfExists(this.file.toPath());
            Files.createFile(this.file.toPath());
            FileOutputStream fos = new FileOutputStream(this.file);
            this.setWriter(new OutputStreamWriter(fos, this.encoding));
            this.writeColumnNames();
            this.getWriter().write(System.getProperty("line.separator"));
        } catch (IOException | NoSuchFieldException | IllegalAccessException var3) {
            throw new DataSetException(var3);
        }
    }

    @Override
    public void reStartTable(ITableMetaData metaData, Integer writeRows) throws DataSetException {
        String activeTableName = metaData.getTableName();

        try {
            this.activeMetaData = metaData;
            this.writeRows = writeRows;
            Field f = CsvDataSetWriter.class.getDeclaredField("_activeMetaData");
            f.setAccessible(true);
            f.set(this, metaData);
            final File directory = new File(this.getTheDirectory());
            this.file = new File(directory, activeTableName + ".csv");
            LOGGER.info("convert - restart fileName={},rows={}", this.file, this.writeRows);
            FileOutputStream fos = new FileOutputStream(this.file, true);
            this.setWriter(new OutputStreamWriter(fos, this.encoding));
        } catch (IOException | NoSuchFieldException | IllegalAccessException var3) {
            throw new DataSetException(var3);
        }
    }

    @Override
    public void row(Object[] objects) throws DataSetException {
        super.row(objects);
        this.writeRows++;
    }

    @Override
    public void endTable() throws DataSetException {
        LOGGER.info("convert - rows={}", this.writeRows);
        LOGGER.info("convert - end   fileName={}", this.file);
        super.endTable();
    }

    @Override
    public void cleanupDirectory() throws DataSetException {
        if (this.resultDir.exists()) {
            try {
                MoreFiles.deleteRecursively(this.resultDir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
            } catch (IOException e) {
                throw new DataSetException(e);
            }
        }
    }

    @Override
    public File getDir() {
        return this.resultDir;
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    private void writeColumnNames() throws DataSetException, IOException {
        LOGGER.debug("writeColumnNames() - start");
        Column[] columns = this.activeMetaData.getColumns();
        for (int i = 0; i < columns.length; ++i) {
            this.getWriter().write(this.quoted(columns[i].getColumnName()));
            if (i < columns.length - 1) {
                this.getWriter().write(",");
            }
        }
    }

    private String quoted(String stringValue) {
        return "\"" + escape(stringValue) + "\"";
    }

}
