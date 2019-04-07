package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.CompareSetting;

import java.io.File;

public class ComparableXlsDataSet extends AbstractComparableDataSet {

    private final File src;

    public ComparableXlsDataSet(File src) throws DataSetException {
        super(new ComparableXlsDataSetProducer(src));
        this.src = src;
    }

    public ComparableXlsDataSet(File aDir, CompareSetting excludeColumns) throws DataSetException {
        super(new ComparableXlsDataSetProducer(aDir), excludeColumns);
        this.src = aDir;
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
