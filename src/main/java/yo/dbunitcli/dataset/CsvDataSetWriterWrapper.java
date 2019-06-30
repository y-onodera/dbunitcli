package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

public class CsvDataSetWriterWrapper implements IDataSetWriter {

    private final CsvDataSetWriter writer;

    public CsvDataSetWriterWrapper(String theDirectory) {
        this.writer = new CsvDataSetWriter(theDirectory);
    }

    public CsvDataSetWriterWrapper(File theDirectory) {
        this.writer = new CsvDataSetWriter(theDirectory);
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        this.writer.write(new DefaultDataSet(new SortedTable(aTable)));
    }
}
