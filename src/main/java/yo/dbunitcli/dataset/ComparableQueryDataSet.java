package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.Parameter;

import java.io.File;

public class ComparableQueryDataSet extends AbstractComparableDataSet {
    private final File src;

    public ComparableQueryDataSet(IDatabaseConnection connection, Parameter parameter, ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableQueryDataSetProducer(connection, param.getSrc(), param.getEncoding(), parameter), param);
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return src.toString();
    }
}
