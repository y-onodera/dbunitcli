package yo.dbunitcli.dataset.converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.stream.IntStream;

public class CsvConverter extends CsvDataSetWriter implements IDataSetConverter {

    private static final Logger LOGGER = LogManager.getLogger();

    private final boolean exportEmptyTable;

    private final File resultDir;

    private final String encoding;

    private ITableMetaData activeMetaData;

    private int writeRows;

    private File file;

    public CsvConverter(final DataSetConsumerParam param) {
        super(param.getResultDir());
        this.resultDir = param.getResultDir();
        this.encoding = param.getOutputEncoding();
        this.exportEmptyTable = param.isExportEmptyTable();
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        final String activeTableName = metaData.getTableName();

        try {
            this.activeMetaData = metaData;
            this.writeRows = 0;
            final Field f = CsvDataSetWriter.class.getDeclaredField("_activeMetaData");
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
            final FileOutputStream fos = new FileOutputStream(this.file);
            this.setWriter(new OutputStreamWriter(fos, this.encoding));
            this.writeColumnNames();
            this.write(System.getProperty("line.separator"));
        } catch (final IOException | NoSuchFieldException | IllegalAccessException var3) {
            throw new DataSetException(var3);
        }
    }

    @Override
    public void reStartTable(final ITableMetaData metaData, final Integer writeRows) throws DataSetException {
        final String activeTableName = metaData.getTableName();

        try {
            this.activeMetaData = metaData;
            this.writeRows = writeRows;
            final Field f = CsvDataSetWriter.class.getDeclaredField("_activeMetaData");
            f.setAccessible(true);
            f.set(this, metaData);
            final File directory = new File(this.getTheDirectory());
            this.file = new File(directory, activeTableName + ".csv");
            LOGGER.info("convert - restart fileName={},rows={}", this.file, this.writeRows);
            final FileOutputStream fos = new FileOutputStream(this.file, true);
            this.setWriter(new OutputStreamWriter(fos, this.encoding));
        } catch (final IOException | NoSuchFieldException | IllegalAccessException var3) {
            throw new DataSetException(var3);
        }
    }

    @Override
    public void row(final Object[] objects) throws DataSetException {
        final Column[] columns = this.activeMetaData.getColumns();
        IntStream.range(0, columns.length).forEach(i -> {
            final String columnName = columns[i].getColumnName();
            final Object value = objects[i];

            if (value == null || value == ITable.NO_VALUE) {
                this.write("");
            } else {
                try {
                    this.write(this.quoted(DataType.asString(value)));
                } catch (final TypeCastException e) {
                    throw new AssertionError("table=" +
                            this.activeMetaData.getTableName() + ", row=" + i +
                            ", column=" + columnName +
                            ", value=" + value, e);
                }
            }
            if (i < columns.length - 1) {
                this.write(",");
            }
        });
        this.write(System.getProperty("line.separator"));
        this.writeRows++;
    }

    @Override
    public void endTable() throws DataSetException {
        LOGGER.info("convert - rows={}", this.writeRows);
        LOGGER.info("convert - end   fileName={}", this.file);
        super.endTable();
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
        final Column[] columns = this.activeMetaData.getColumns();
        IntStream.range(0, columns.length).forEach(i -> {
            this.write(this.quoted(columns[i].getColumnName()));
            if (i < columns.length - 1) {
                this.write(",");
            }
        });
    }

    private String quoted(final String stringValue) {
        return "\"" + escape(stringValue) + "\"";
    }

    private void write(final String s) {
        try {
            this.getWriter().write(s);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
