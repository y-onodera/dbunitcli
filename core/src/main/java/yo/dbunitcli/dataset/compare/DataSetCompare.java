package yo.dbunitcli.dataset.compare;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetProducer;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static yo.dbunitcli.dataset.compare.CompareDiff.getBuilder;

public class DataSetCompare implements Compare {

    private static final String RESULT_TABLE_NAME = "COMPARE_RESULT";

    private final ComparableDataSet oldDataSet;

    private final ComparableDataSet newDataSet;

    private final AddSettingColumns comparisonKeys;

    private final IDataSetConverter writer;

    private final TableDataSetCompare tableCompare;

    private final CompareResult result;

    public DataSetCompare(DataSetCompareBuilder builder) throws DataSetException {
        this.oldDataSet = builder.getOldDataSet();
        this.newDataSet = builder.getNewDataSet();
        this.comparisonKeys = builder.getComparisonKeys();
        this.tableCompare = builder.getTableDataSetCompare();
        this.writer = builder.getDataSetWriter();
        this.result = this.exec();
    }

    @Override
    public CompareResult result() throws DataSetException {
        return this.result;
    }

    protected CompareResult exec() throws DataSetException {
        this.writer.cleanupDirectory();
        List<CompareDiff> results = Lists.newArrayList();
        results.addAll(this.compareTableCount());
        results.addAll(this.compareTables(this.writer));
        CompareResult compareResult = new CompareResult(this.oldDataSet.getSrc(), this.newDataSet.getSrc(), results);
        final ITable table = compareResult.toITable(RESULT_TABLE_NAME);
        this.writer.open(table.getTableMetaData().getTableName());
        IDataSetProducer producer = new DataSetProducerAdapter(new DefaultDataSet(table));
        producer.setConsumer(this.writer);
        producer.produce();
        return compareResult;
    }

    protected List<CompareDiff> compareTableCount() throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        final int oldTableCounts = this.oldDataSet.getTableNames().length;
        final int newTableCounts = this.newDataSet.getTableNames().length;
        if (oldTableCounts != newTableCounts) {
            results.add(getBuilder(CompareDiff.Type.TABLE_COUNT)
                    .setOldDef(String.valueOf(oldTableCounts))
                    .setNewDef(String.valueOf(newTableCounts))
                    .build());
        }
        return results;
    }

    protected List<CompareDiff> compareTables(IDataSetConverter writer) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        Set<String> oldTables = Sets.newHashSet(this.oldDataSet.getTableNames());
        Set<String> newTables = Sets.newHashSet(this.newDataSet.getTableNames());
        results.addAll(this.searchDeleteTables(oldTables, newTables));
        results.addAll(this.searchAddTables(oldTables, newTables));
        results.addAll(this.searchModifyTables(oldTables, newTables));
        return results;
    }

    protected List<CompareDiff> searchAddTables(Set<String> oldTables, Set<String> newTables) {
        List<CompareDiff> results = Lists.newArrayList();
        newTables.stream()
                .filter(it -> !Predicates.in(oldTables).apply(it))
                .collect(Collectors.toSet())
                .forEach(name -> results.add(getBuilder(CompareDiff.Type.TABLE_ADD)
                        .setTargetName(name)
                        .setNewDef(name)
                        .build()));
        return results;
    }

    protected List<CompareDiff> searchDeleteTables(Set<String> oldTables, Set<String> newTables) {
        List<CompareDiff> results = Lists.newArrayList();
        oldTables.stream()
                .filter(it -> !Predicates.in(newTables).apply(it))
                .collect(Collectors.toSet())
                .forEach(name -> results.add(getBuilder(CompareDiff.Type.TABLE_DELETE)
                        .setTargetName(name)
                        .setOldDef(name)
                        .build()));
        return results;
    }

    protected List<CompareDiff> searchModifyTables(Set<String> oldTables, Set<String> newTables) throws DataSetException {
        List<CompareDiff> results = Lists.newArrayList();
        for (String tableName : Sets.intersection(oldTables, newTables)) {
            ComparableTable oldTable = this.oldDataSet.getTable(tableName);
            ComparableTable newTable = this.newDataSet.getTable(tableName);
            results.addAll(this.tableCompare.getResults(oldTable, newTable, this.comparisonKeys, this.writer));
        }
        return results;
    }

}
