package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableXlsDataSet extends AbstractComparableDataSet {

    private final File src;

    public ComparableXlsDataSet(File src) throws DataSetException {
        this(ComparableDataSetLoaderParam.builder().setSrc(src).build());
    }

    public ComparableXlsDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableXlsDataSetProducer(param), param);
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return this.src.toString();
    }
}
