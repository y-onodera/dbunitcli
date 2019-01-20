package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableXlsxDataSet extends AbstractComparableDataSet {
    private File src;

    public ComparableXlsxDataSet(File source) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(source));
        this.src = source;
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
