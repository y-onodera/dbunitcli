package yo.dbunitcli.dataset;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;

public class ComparableDBDataSet extends AbstractComparableDataSet {
    private final File src;

    public ComparableDBDataSet(IDatabaseConnection connection, File aSrc, String aEncoding) throws DataSetException {
        super(new ComparableDBDataSetProducer(connection, aSrc, aEncoding));
        this.src = aSrc;
    }

    public ComparableDBDataSet(IDatabaseConnection connection, File aSrc, String aEncoding, ColumnSetting excludeColumns) throws DataSetException {
        super(new ComparableDBDataSetProducer(connection, aSrc, aEncoding), excludeColumns);
        this.src = aSrc;
    }

    @Override
    public String getSrc() {
        return src.toString();
    }

}
