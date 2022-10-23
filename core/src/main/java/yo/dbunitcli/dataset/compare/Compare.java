package yo.dbunitcli.dataset.compare;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetProducer;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Compare {

    private final ComparableDataSet oldDataSet;

    private final ComparableDataSet newDataSet;

    private final AddSettingColumns comparisonKeys;

    private final IDataSetConverter writer;

    private final Manager manager;

    public Compare(CompareBuilder builder) {
        this.oldDataSet = builder.getOldDataSet();
        this.newDataSet = builder.getNewDataSet();
        this.comparisonKeys = builder.getComparisonKeys();
        this.writer = builder.getDataSetWriter();
        this.manager = builder.getManager();
    }

    public CompareResult result() throws DataSetException {
        this.cleanupDirectory(this.writer.getDir());
        CompareResult compareResult = this.manager.getResult(this);
        IDataSetProducer producer = new DataSetProducerAdapter(new DefaultDataSet(compareResult.toITable()));
        producer.setConsumer(this.writer);
        producer.produce();
        return compareResult;
    }

    protected void cleanupDirectory(File dir) {
        if (dir.exists()) {
            Delete delete = new Delete();
            Project project = new Project();
            project.setName("dbunit-cli");
            project.setBaseDir(new File("."));
            project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
            delete.setProject(project);
            delete.setDir(dir);
            delete.execute();
        }
    }

    public ComparableDataSet getOldDataSet() {
        return oldDataSet;
    }

    public ComparableDataSet getNewDataSet() {
        return newDataSet;
    }

    public AddSettingColumns getComparisonKeys() {
        return comparisonKeys;
    }

    public IDataSetConverter getWriter() {
        return writer;
    }

    public interface Manager {

        default CompareResult getResult(Compare compare) {
            List<CompareDiff> results = Lists.newArrayList();
            getStrategies().forEach(it -> results.addAll(it.apply(compare)));
            return this.toCompareResult(compare.getOldDataSet(), compare.getNewDataSet(), results);
        }

        default Stream<Function<Compare, List<CompareDiff>>> getStrategies() {
            return Stream.of(this.compareTableCount()
                    , this.searchDeleteTables()
                    , this.searchAddTables()
                    , this.searchModifyTables());
        }

        default Function<Compare, List<CompareDiff>> compareTableCount() {
            return (it) -> {
                List<CompareDiff> results = Lists.newArrayList();
                try {
                    final int oldTableCounts = it.getOldDataSet().getTableNames().length;
                    final int newTableCounts = it.getNewDataSet().getTableNames().length;
                    if (oldTableCounts != newTableCounts) {
                        results.add(CompareDiff.Type.TABLE_COUNT.of()
                                .setOldDefine(String.valueOf(oldTableCounts))
                                .setNewDefine(String.valueOf(newTableCounts))
                                .build());
                    }
                } catch (DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        default Function<Compare, List<CompareDiff>> searchAddTables() {
            return (it) -> {
                List<CompareDiff> results = Lists.newArrayList();
                try {
                    Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    newTables.stream()
                            .filter(table -> !Predicates.in(oldTables).apply(table))
                            .collect(Collectors.toSet())
                            .forEach(name -> results.add(CompareDiff.Type.TABLE_ADD.of()
                                    .setTargetName(name)
                                    .setNewDefine(name)
                                    .build()));
                } catch (DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        default Function<Compare, List<CompareDiff>> searchDeleteTables() {
            return (it) -> {
                List<CompareDiff> results = Lists.newArrayList();
                try {
                    Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    oldTables.stream()
                            .filter(table -> !Predicates.in(newTables).apply(table))
                            .collect(Collectors.toSet())
                            .forEach(name -> results.add(CompareDiff.Type.TABLE_DELETE.of()
                                    .setTargetName(name)
                                    .setOldDefine(name)
                                    .build()));
                } catch (DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        default Function<Compare, List<CompareDiff>> searchModifyTables() {
            return (it) -> {
                List<CompareDiff> results = Lists.newArrayList();
                try {
                    Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    for (String tableName : Sets.intersection(oldTables, newTables)) {
                        ComparableTable oldTable = it.getOldDataSet().getTable(tableName);
                        ComparableTable newTable = it.getNewDataSet().getTable(tableName);
                        results.addAll(this.compareTable(oldTable, newTable, it.getComparisonKeys(), it.getWriter()));
                    }
                } catch (DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        List<CompareDiff> compareTable(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter writer) throws DataSetException;

        CompareResult toCompareResult(ComparableDataSet oldDataSet, ComparableDataSet newDataSet, List<CompareDiff> results);
    }

}
