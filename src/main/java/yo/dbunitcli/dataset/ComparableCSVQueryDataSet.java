package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;
import java.util.Map;

public class ComparableCSVQueryDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableCSVQueryDataSet(File aDir, String aEncoding, ColumnSetting excludeColumns, Map<String, Object> parameter) throws DataSetException {
        super(new ComparableCSVQueryDataSetProducer(aDir, aEncoding, parameter), excludeColumns);
        this.srcDir = aDir.getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}

