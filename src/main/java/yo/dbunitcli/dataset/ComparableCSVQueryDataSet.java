package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;

public class ComparableCSVQueryDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableCSVQueryDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        super(new ComparableCSVQueryDataSetProducer(aSrcDir, aEncoding));
        this.srcDir = aSrcDir.getPath();
    }

    public ComparableCSVQueryDataSet(File aDir, String aEncoding, ColumnSetting excludeColumns) throws DataSetException {
        super(new ComparableCSVQueryDataSetProducer(aDir, aEncoding), excludeColumns);
        this.srcDir = aDir.getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}

