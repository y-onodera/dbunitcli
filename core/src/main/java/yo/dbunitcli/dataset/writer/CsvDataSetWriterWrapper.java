package yo.dbunitcli.dataset.writer;

import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.DataSetWriterParam;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;

public class CsvDataSetWriterWrapper implements IDataSetWriter {

    private final CsvDataSetWriter writer;

    private final boolean exportEmptyTable;

    public CsvDataSetWriterWrapper(DataSetWriterParam param) {
        this.writer = new ExCsvDataSetWriter(param.getResultDir(), param.getOutputEncoding());
        this.exportEmptyTable = param.isExportEmptyTable();
    }

    @Override
    public void cleanupDirectory() {
        final File directory = new File(this.writer.getTheDirectory());
        if (directory.exists()) {
            directory.delete();
        }
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        if (!this.exportEmptyTable && aTable.getRowCount() == 0) {
            return;
        }
        this.writer.write(new DefaultDataSet(aTable));
    }

    private static class ExCsvDataSetWriter extends CsvDataSetWriter {

        private static final Logger logger = LoggerFactory.getLogger(CsvDataSetWriter.class);

        private String activeTableName;

        private final String encoding;

        private ITableMetaData activeMetaData;

        public ExCsvDataSetWriter(File theDirectory, String encoding) {
            super(theDirectory);
            this.encoding = encoding;
        }

        @Override
        public void startTable(ITableMetaData metaData) throws DataSetException {
            this.activeTableName = metaData.getTableName();

            try {
                this.activeMetaData = metaData;
                Field f = CsvDataSetWriter.class.getDeclaredField("_activeMetaData");
                f.setAccessible(true);
                f.set(this, metaData);
                final File directory = new File(this.getTheDirectory());
                File file = new File(directory, this.activeTableName + ".csv");
                logger.info("writeToFile(fileName={}) - start", file);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                this.setWriter(new OutputStreamWriter(fos, this.encoding));
                this.writeColumnNames();
                this.getWriter().write(System.getProperty("line.separator"));
            } catch (IOException | NoSuchFieldException | IllegalAccessException var3) {
                throw new DataSetException(var3);
            }
        }

        private void writeColumnNames() throws DataSetException, IOException {
            logger.debug("writeColumnNames() - start");
            Column[] columns = this.activeMetaData.getColumns();

            for (int i = 0; i < columns.length; ++i) {
                String columnName = columns[i].getColumnName();
                this.getWriter().write(columnName);
                if (i < columns.length - 1) {
                    this.getWriter().write(",");
                }
            }

        }
    }
}
