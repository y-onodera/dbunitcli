package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableXlsDataSet extends AbstractComparableDataSet{

    private final File src;

    public ComparableXlsDataSet(File src) throws DataSetException {
        super(new ComparableXlsDataSetProducer(src));
        this.src = src;
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
