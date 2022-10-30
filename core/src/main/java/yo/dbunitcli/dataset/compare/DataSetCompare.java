package yo.dbunitcli.dataset.compare;

import com.google.common.base.Predicates;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSetCompare {

    private final ComparableDataSet oldDataSet;

    private final ComparableDataSet newDataSet;

    private final AddSettingColumns comparisonKeys;

    private final IDataSetConverter converter;

    private final Manager manager;

    public DataSetCompare(DataSetCompareBuilder builder) {
        this.oldDataSet = builder.getOldDataSet();
        this.newDataSet = builder.getNewDataSet();
        this.comparisonKeys = builder.getComparisonKeys();
        this.converter = builder.getDataSetConverter();
        this.manager = builder.getManager();
    }

    public CompareResult result() throws DataSetException {
        this.cleanupDirectory(this.converter.getDir());
        CompareResult compareResult = this.manager.exec(this);
        IDataSetProducer producer = new DataSetProducerAdapter(new DefaultDataSet(compareResult.toITable()));
        producer.setConsumer(this.converter);
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

    public IDataSetConverter getConverter() {
        return converter;
    }

    public interface Manager {

        default CompareResult exec(DataSetCompare dataSetCompare) {
            List<CompareDiff> results = new ArrayList<>();
            getStrategies().forEach(it -> results.addAll(it.apply(dataSetCompare)));
            return this.toCompareResult(dataSetCompare.getOldDataSet(), dataSetCompare.getNewDataSet(), results);
        }

        default Stream<Function<DataSetCompare, List<CompareDiff>>> getStrategies() {
            return Stream.of(this.compareTableCount()
                    , this.searchDeleteTables()
                    , this.searchAddTables()
                    , this.searchModifyTables());
        }

        default Function<DataSetCompare, List<CompareDiff>> compareTableCount() {
            return (it) -> {
                List<CompareDiff> results = new ArrayList<>();
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

        default Function<DataSetCompare, List<CompareDiff>> searchAddTables() {
            return (it) -> {
                List<CompareDiff> results = new ArrayList<>();
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

        default Function<DataSetCompare, List<CompareDiff>> searchDeleteTables() {
            return (it) -> {
                List<CompareDiff> results = new ArrayList<>();
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

        default Function<DataSetCompare, List<CompareDiff>> searchModifyTables() {
            return (it) -> {
                List<CompareDiff> results = new ArrayList<>();
                try {
                    Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    for (String tableName : Sets.intersection(oldTables, newTables)) {
                        ComparableTable oldTable = it.getOldDataSet().getTable(tableName);
                        ComparableTable newTable = it.getNewDataSet().getTable(tableName);
                        if (it.getComparisonKeys().hasAdditionalSetting(oldTable.getTableMetaData().getTableName())) {
                            results.addAll(this.compareTable(new TableCompare(oldTable, newTable, it.getComparisonKeys(), it.getConverter())));
                        }
                    }
                } catch (DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        List<CompareDiff> compareTable(TableCompare tableCompare) throws DataSetException;

        CompareResult toCompareResult(ComparableDataSet oldDataSet, ComparableDataSet newDataSet, List<CompareDiff> results);
    }

    static class TableCompare {
        private final ComparableTable oldTable;
        private final ComparableTable newTable;
        private final AddSettingColumns comparisonKeys;
        private final IDataSetConverter converter;
        private final int columnLength;
        private final List<String> keyColumns;

        public TableCompare(ComparableTable oldTable, ComparableTable newTable, AddSettingColumns comparisonKeys, IDataSetConverter converter) {
            this.oldTable = oldTable;
            this.newTable = newTable;
            this.comparisonKeys = comparisonKeys;
            this.converter = converter;
            this.columnLength = Math.min(this.getOldColumnLength(), this.getNewColumnLength());
            this.keyColumns = this.comparisonKeys.getColumns(this.oldTable.getTableMetaData().getTableName());
        }

        public int getNewColumnLength() {
            return this.getColumnLength(this.newTable);
        }

        public int getOldColumnLength() {
            return this.getColumnLength(this.oldTable);
        }

        public ComparableTable getOldTable() {
            return oldTable;
        }

        public ComparableTable getNewTable() {
            return newTable;
        }

        public int getColumnLength() {
            return columnLength;
        }

        public List<String> getKeyColumns() {
            return keyColumns;
        }

        public AddSettingColumns getComparisonKeys() {
            return comparisonKeys;
        }

        public IDataSetConverter getConverter() {
            return converter;
        }

        protected int getColumnLength(ComparableTable table) {
            try {
                return table.getTableMetaData().getColumns().length;
            } catch (DataSetException e) {
                throw new AssertionError(e);
            }
        }

    }
}
