package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableXlsDataSet extends AbstractComparableDataSet {

    private final File src;

    public ComparableXlsDataSet(File src) throws DataSetException {
        super(new ComparableXlsDataSetProducer(src));
        this.src = src;
    }

    public ComparableXlsDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableXlsDataSetProducer(param.getSrc())
                , param.getExcludeColumns()
                , param.getOrderColumns());
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
