package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

public class CsvDataSetWriterWrapper implements IDataSetWriter {

    private final CsvDataSetWriter writer;

    public CsvDataSetWriterWrapper(String theDirectory, String encoding) {
        this.writer = new ExCsvDataSetWriter(theDirectory, encoding);
    }

    public CsvDataSetWriterWrapper(File theDirectory, String encoding) {
        this.writer = new ExCsvDataSetWriter(theDirectory, encoding);
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        this.writer.write(new DefaultDataSet(new SortedTable(aTable)));
    }

    private static class ExCsvDataSetWriter extends CsvDataSetWriter {

        private static final Logger logger = LoggerFactory.getLogger(CsvDataSetWriter.class);

        private String activeTableName;

        private final String encoding;

        private ITableMetaData activeMetaData;

        public ExCsvDataSetWriter(String theDirectory, String encoding) {
            super(theDirectory);
            this.encoding = encoding;
        }

        public ExCsvDataSetWriter(File theDirectory, String encoding) {
            super(theDirectory);
            this.encoding = encoding;
        }

        @Override
        public void startTable(ITableMetaData metaData) throws DataSetException {
            this.activeTableName = metaData.getTableName();
            logger.debug("startTable(metaData={}) - start", metaData);

            try {
                this.activeMetaData = metaData;
                Field f = CsvDataSetWriter.class.getDeclaredField("_activeMetaData");
                f.setAccessible(true);
                f.set(this, metaData);
                this.setWriter(new FileWriter(this.getTheDirectory() + File.separator + metaData.getTableName() + ".csv"));
                this.writeColumnNames();
                this.getWriter().write(System.getProperty("line.separator"));
            } catch (IOException | NoSuchFieldException | IllegalAccessException var3) {
                throw new DataSetException(var3);
            }
        }

        @Override
        public void setWriter(Writer writer) {
            final File directory = new File(this.getTheDirectory());
            try {
                writer.close();
                File f = new File(directory, this.activeTableName + ".csv");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                super.setWriter(new OutputStreamWriter(fos, this.encoding));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public void row(Object[] values) throws DataSetException {
            Object[] result = new Object[values.length];
            int i = 0;
            for (Object value : values) {
                if (value != null) {
                    result[i] = value;
                } else {
                    result[i] = "";
                }
                i++;
            }
            super.row(result);
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
