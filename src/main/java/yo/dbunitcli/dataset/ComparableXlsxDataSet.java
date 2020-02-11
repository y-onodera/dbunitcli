package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableXlsxDataSet extends AbstractComparableDataSet {
    private File src;

    public ComparableXlsxDataSet(File source) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(source));
        this.src = source;
    }

    public ComparableXlsxDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(param.getSrc())
                , param.getExcludeColumns()
                , param.getOrderColumns());
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
