package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSetWriter;

public class CsvDataSetWriterWrapper implements IDataSetWriter {

    private final CsvDataSetWriter writer;

    public CsvDataSetWriterWrapper(CsvDataSetWriter writer) {
        this.writer = writer;
    }

    @Override
    public void write(ITable aTable) throws DataSetException {
        this.writer.write(new DefaultDataSet(aTable));
    }
}
