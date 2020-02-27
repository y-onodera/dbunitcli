package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.DataSourceType;

public class ComparableFileDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableFileDataSet(ComparableDataSetLoaderParam param) throws DataSetException {
        super(getProducer(param), param);
        this.srcDir = param.getSrc().getPath();
    }

    protected static ComparableFileDataSetProducer getProducer(ComparableDataSetLoaderParam param) {
        if (param.getSource() == DataSourceType.DIR) {
            return new ComparableDirectoryDataSetProducer(param);
        }
        return new ComparableFileDataSetProducer(param);
    }

    @Override
    public String getSrc() {
        return this.srcDir;
    }

}
