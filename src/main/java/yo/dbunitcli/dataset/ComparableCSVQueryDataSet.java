package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.Parameter;

public class ComparableCSVQueryDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableCSVQueryDataSet(ComparableDataSetLoaderParam param, Parameter parameter) throws DataSetException {
        super(new ComparableCSVQueryDataSetProducer(param, parameter), param);
        this.srcDir = param.getSrc().getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}

