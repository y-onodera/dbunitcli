package yo.dbunitcli.dataset.consumer;

import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetConsumerParam;
import yo.dbunitcli.dataset.IDataSetConsumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;

public class CsvConsumer implements IDataSetConsumer {

    private final CsvDataSetWriter writer;

    private final boolean exportEmptyTable;

    private final File resultDir;

    public CsvConsumer(DataSetConsumerParam param) {
        this.resultDir = param.getResultDir();
        this.writer = new ExCsvDataSetWriter(this.resultDir, param.getOutputEncoding());
        this.exportEmptyTable = param.isExportEmptyTable();
    }

    @Override
    public void startDataSet() throws DataSetException {
        this.writer.startDataSet();
    }

    @Override
    public void startTable(ITableMetaData iTableMetaData) throws DataSetException {
        this.writer.startTable(iTableMetaData);
    }

    @Override
    public void row(Object[] objects) throws DataSetException {
        this.writer.row(objects);
    }

    @Override
    public void endTable() throws DataSetException {
        this.writer.endTable();
    }

    @Override
    public void endDataSet() throws DataSetException {
        this.writer.endDataSet();
    }

    @Override
    public void cleanupDirectory() {
        final File directory = new File(this.writer.getTheDirectory());
        if (directory.exists()) {
            directory.delete();
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

    private static class ExCsvDataSetWriter extends CsvDataSetWriter {

        private static final Logger LOGGER = LoggerFactory.getLogger(ExCsvDataSetWriter.class);

        private final String encoding;

        private ITableMetaData activeMetaData;

        private int writeRows;

        private File file;

        public ExCsvDataSetWriter(File theDirectory, String encoding) {
            super(theDirectory);
            this.encoding = encoding;
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
                LOGGER.info("consume - start fileName={}", this.file);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (this.file.exists()) {
                    this.file.delete();
                }
                this.file.createNewFile();
                FileOutputStream fos = new FileOutputStream(this.file);
                this.setWriter(new OutputStreamWriter(fos, this.encoding));
                this.writeColumnNames();
                this.getWriter().write(System.getProperty("line.separator"));
            } catch (IOException | NoSuchFieldException | IllegalAccessException var3) {
                throw new DataSetException(var3);
            }
        }

        @Override
        public void row(Object[] values) throws DataSetException {
            super.row(values);
            this.writeRows++;
        }

        @Override
        public void endTable() throws DataSetException {
            LOGGER.info("consume - rows={}", this.writeRows);
            LOGGER.info("consume - end   fileName={}", this.file);
            super.endTable();
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
}
