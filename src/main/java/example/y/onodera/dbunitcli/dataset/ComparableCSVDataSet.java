package example.y.onodera.dbunitcli.dataset;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.dbunit.dataset.common.handlers.IllegalInputCharacterException;
import org.dbunit.dataset.common.handlers.PipelineException;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.csv.CsvParserImpl;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
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
        super(new ComparableCsvDataSetProducer(aSrcDir,aEncoding));
        this.srcDir = aSrcDir.getAbsolutePath();
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
        List<CompareDiff> results = Lists.newArrayList();
        Set<String> oldTables = Sets.newHashSet(this.getTableNames());
        Set<String> newTables = Sets.newHashSet(newData.getTableNames());
        results.addAll(CompareDiff.deleteTable(Sets.filter(oldTables, Predicates.not(Predicates.in(newTables)))));
        results.addAll(CompareDiff.addTable(Sets.filter(newTables, Predicates.not(Predicates.in(oldTables)))));
        for (String tableName : Sets.intersection(oldTables, newTables)) {
            ComparableTable oldTable = this.getTable(tableName);
            ComparableTable newTable = newData.getTable(tableName);
            results.addAll(oldTable.compareSchema(newTable, comparisonKeys));
        }
        return new CompareResult(this.getSrcDir(), newData.getSrcDir(), results);
    }
}
