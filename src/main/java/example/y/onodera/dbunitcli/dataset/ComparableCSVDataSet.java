package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.*;
import org.dbunit.dataset.filter.IColumnFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class ComparableCSVDataSet extends AbstractComparableDataSet {
    private static final Logger logger = LoggerFactory.getLogger(ComparableCSVDataSet.class);

    private final String srcDir;

    public ComparableCSVDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcDir, aEncoding), Maps.newHashMap());
        this.srcDir = aSrcDir.getPath();
    }

    public ComparableCSVDataSet(File aSrcFile) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcFile), Maps.newHashMap());
        this.srcDir = aSrcFile.getPath();
    }

    public ComparableCSVDataSet(File aDir, String aEncoding, Map<String, IColumnFilter> excludeColumns) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aDir, aEncoding), excludeColumns);
        this.srcDir = aDir.getPath();
    }

    @Override
    public String getSrc() {
        return srcDir;
    }

}
