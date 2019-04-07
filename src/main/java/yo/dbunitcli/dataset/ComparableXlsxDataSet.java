package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.CompareSetting;

import java.io.File;

public class ComparableXlsxDataSet extends AbstractComparableDataSet {
    private File src;

    public ComparableXlsxDataSet(File source) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(source));
        this.src = source;
    }

    public ComparableXlsxDataSet(File aFile, CompareSetting excludeColumns) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(aFile), excludeColumns);
        this.src = aFile;
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
