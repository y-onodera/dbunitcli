package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSetWriter;

import java.io.*;

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

        private String activeTableName;

        private final String encoding;

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
            super.startTable(metaData);
        }

        @Override
        public void setWriter(Writer writer) {
            final File directory = new File(this.getTheDirectory());
            try {
                File f = new File(directory, this.activeTableName + ".csv");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                writer.close();
                super.setWriter(new OutputStreamWriter(fos, this.encoding));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }
}
