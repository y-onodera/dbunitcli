package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;

import java.io.File;

public class ComparableDBDataSet extends AbstractComparableDataSet {

    private final File src;

    public ComparableDBDataSet(IDatabaseConnection connection, ComparableDataSetLoaderParam param) throws DataSetException {
        super(new ComparableDBDataSetProducer(connection, param.getSrc(), param.getEncoding()), param);
        this.src = param.getSrc();
    }

    @Override
    public String getSrc() {
        return src.toString();
    }

}
