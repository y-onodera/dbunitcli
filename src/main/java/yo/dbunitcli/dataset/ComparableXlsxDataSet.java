package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableXlsxDataSet extends AbstractComparableDataSet {
    private File src;

    public ComparableXlsxDataSet(File src) throws DataSetException {
        this(ComparableDataSetLoaderParam.builder().setSrc(src).build());
    }

    public ComparableXlsxDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableXlsxDataSetProducer(param), param);
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
