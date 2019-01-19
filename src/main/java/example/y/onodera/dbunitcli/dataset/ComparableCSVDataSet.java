package example.y.onodera.dbunitcli.dataset;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComparableCSVDataSet extends CachedDataSet {
    private static final Logger logger = LoggerFactory.getLogger(ComparableCSVDataSet.class);

    private final String srcDir;

    public ComparableCSVDataSet(File aSrcDir, String aEncoding) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcDir, aEncoding));
        this.srcDir = aSrcDir.getPath();
    }

    public ComparableCSVDataSet(File aSrcFile) throws DataSetException {
        super(new ComparableCsvDataSetProducer(aSrcFile));
        this.srcDir = aSrcFile.getParent();
    }

    public String getSrcDir() {
        return srcDir;
    }

    @Override
    public ComparableTable getTable(String tableName) throws DataSetException {
        return new ComparableTable(super.getTable(tableName));
    }

    public CompareResult compare(ComparableCSVDataSet newData, Map<String, List<String>> comparisonKeys) throws DataSetException {
        return new CompareResult(this.getSrcDir(), newData.getSrcDir(), CompareDiff.tables(this, newData, comparisonKeys));
    }
}
