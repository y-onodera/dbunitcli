package yo.dbunitcli.dataset.converter;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.AddSettingTableMetaData;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public abstract class FlatFileConverter implements IDataSetConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlatFileConverter.class);
    protected final boolean exportEmptyTable;
    protected final String theDirectory;
    protected final File resultDir;
    protected final String encoding;
    protected final boolean exportHeader;
    protected final Charset charset;
    protected final String extension;
    protected ITableMetaData activeMetaData;
    protected int writeRows;
    protected File file;
    protected Writer writer;

    public FlatFileConverter(final String theDirectory, final File resultDir, final String encoding,
                             final boolean exportEmptyTable, final boolean exportHeader, final String extension) {
        this.exportEmptyTable = exportEmptyTable;
        this.theDirectory = theDirectory;
        this.resultDir = resultDir;
        this.encoding = encoding;
        this.exportHeader = exportHeader;
        this.charset = Charset.forName(this.encoding);
        this.extension = extension;
    }

    @Override
    public File getDir() {
        return this.resultDir;
    }

    @Override
    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        final String activeTableName = metaData.getTableName();

        try {
            this.activeMetaData = metaData;
            this.writeRows = 0;
            final File directory = new File(this.theDirectory);
            this.file = new File(directory, activeTableName + "." + this.getExtension());
            LOGGER.info("convert - start fileName={}", this.file);
            if (!directory.exists()) {
                Files.createDirectories(directory.toPath());
            }
            Files.deleteIfExists(this.file.toPath());
            Files.createFile(this.file.toPath());
            final FileOutputStream fos = new FileOutputStream(this.file);
            this.writer = new OutputStreamWriter(fos, this.encoding);
            if (this.exportHeader) {
                this.writeHeader();
            }
        } catch (final IOException var3) {
            throw new DataSetException(var3);
        }
    }

    @Override
    public void reStartTable(AddSettingTableMetaData tableMetaData, Integer writeRows) {
        try {
            this.activeMetaData = tableMetaData;
            this.writeRows = writeRows;
            final File directory = new File(this.theDirectory);
            this.file = new File(directory, tableMetaData.getTableName() + "." + this.getExtension());
            LOGGER.info("convert - restart fileName={},rows={}", this.file, this.writeRows);
            final FileOutputStream fos = new FileOutputStream(this.file, true);
            this.writer = new OutputStreamWriter(fos, this.encoding);
        } catch (final IOException var3) {
            throw new AssertionError(var3);
        }
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

    protected String getExtension() {
        return this.extension;
    }

    protected void writeHeader() {
    }

    protected Column[] getColumns() {
        try {
            return this.activeMetaData.getColumns();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void write(final String s) {
        try {
            this.writer.write(s);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
    }

}
