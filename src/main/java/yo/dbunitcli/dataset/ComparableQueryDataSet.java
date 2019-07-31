package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.application.Parameter;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;

public class ComparableQueryDataSet extends AbstractComparableDataSet {
    private final File src;

    public ComparableQueryDataSet(IDatabaseConnection connection, File aSrc, String aEncoding, ColumnSetting excludeColumns, Parameter parameter) throws DataSetException {
        super(new ComparableQueryDataSetProducer(connection, aSrc, aEncoding, parameter), excludeColumns);
        this.src = aSrc;
    }

    @Override
    public String getSrc() {
        return src.toString();
    }
}
