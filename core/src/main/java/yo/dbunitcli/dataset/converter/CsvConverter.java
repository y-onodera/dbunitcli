package yo.dbunitcli.dataset.converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.IntStream;

public class CsvConverter implements IDataSetConverter {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String QUOTE = "\"";
    private static final String ESCAPE = "\\";

    private final boolean exportEmptyTable;

    private final File resultDir;

    private final String encoding;

    private ITableMetaData activeMetaData;

    private int writeRows;

    private File file;

    private final String theDirectory;

    private Writer writer;

    public CsvConverter(final DataSetConsumerParam param) {
        this(param.getResultDir().getAbsolutePath()
                , param.getResultDir()
                , param.getOutputEncoding()
                , param.isExportEmptyTable());
    }

    public CsvConverter(final String theDirectory, final File resultDir, final String encoding, final boolean exportEmptyTable) {
        this.theDirectory = theDirectory;
        this.resultDir = resultDir;
        this.encoding = encoding;
        this.exportEmptyTable = exportEmptyTable;
    }

    @Override
    public void startDataSet() throws DataSetException {

    }

    @Override
    public void endDataSet() throws DataSetException {

    }

    @Override
    public IDataSetConverter split() {
        return new CsvConverter(this.theDirectory, this.resultDir, this.encoding, this.exportEmptyTable);
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        final String activeTableName = metaData.getTableName();

        try {
            this.activeMetaData = metaData;
            this.writeRows = 0;
            final File directory = new File(this.theDirectory);
            this.file = new File(directory, activeTableName + ".csv");
            LOGGER.info("convert - start fileName={}", this.file);
            if (!directory.exists()) {
                Files.createDirectories(directory.toPath());
            }
            Files.deleteIfExists(this.file.toPath());
            Files.createFile(this.file.toPath());
            final FileOutputStream fos = new FileOutputStream(this.file);
            this.writer = new OutputStreamWriter(fos, this.encoding);
            this.writeColumnNames();
            this.write(System.getProperty("line.separator"));
        } catch (final IOException var3) {
            throw new DataSetException(var3);
        }
    }

    @Override
    public void reStartTable(final ITableMetaData metaData, final Integer writeRows) {
        try {
            this.activeMetaData = metaData;
            this.writeRows = writeRows;
            final File directory = new File(this.theDirectory);
            this.file = new File(directory, metaData.getTableName() + ".csv");
            LOGGER.info("convert - restart fileName={},rows={}", this.file, this.writeRows);
            final FileOutputStream fos = new FileOutputStream(this.file, true);
            this.writer = new OutputStreamWriter(fos, this.encoding);
        } catch (final IOException var3) {
            throw new AssertionError(var3);
        }
    }

    @Override
    public void row(final Object[] objects) throws DataSetException {
        final Column[] columns = this.getColumns();
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
        try {
            this.writer.close();
        } catch (final IOException var3) {
            throw new AssertionError(var3);
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

    protected void writeColumnNames() {
        LOGGER.debug("writeColumnNames() - start");
        final Column[] columns = this.getColumns();
        IntStream.range(0, columns.length).forEach(i -> {
            this.write(this.quoted(columns[i].getColumnName()));
            if (i < columns.length - 1) {
                this.write(",");
            }
        });
    }

    protected Column[] getColumns() {
        try {
            return this.activeMetaData.getColumns();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected String quoted(final String stringValue) {
        return "\"" + this.escape(stringValue) + "\"";
    }

    protected String escape(final String stringValue) {
        final char[] array = stringValue.toCharArray();
        final char testExport = QUOTE.toCharArray()[0];
        final char escape = ESCAPE.toCharArray()[0];
        final StringBuilder buffer = new StringBuilder();
        for (final char c : array) {
            if (c == testExport || c == escape) {
                buffer.append('\\');
            }
            buffer.append(c);
        }
        return buffer.toString();
    }

    protected void write(final String s) {
        try {
            this.writer.write(s);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }
}
