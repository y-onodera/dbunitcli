package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.compare.CompareSetting;

import java.io.File;

public class ComparableCSVDataSet extends AbstractComparableDataSet {

    private final String srcDir;

    public ComparableCSVDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcDir, aEncoding));
        this.srcDir = aSrcDir.getPath();
    }

    public ComparableCSVDataSet(File aSrcFile) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcFile));
        this.srcDir = aSrcFile.getPath();
    }

    public ComparableCSVDataSet(File aDir, String aEncoding, CompareSetting excludeColumns) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aDir, aEncoding), excludeColumns);
        this.srcDir = aDir.getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}
