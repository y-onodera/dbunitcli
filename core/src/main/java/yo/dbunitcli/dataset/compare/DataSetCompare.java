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

    public DataSetCompare(final DataSetCompareBuilder builder) {
        this.oldDataSet = builder.getOldDataSet();
        this.newDataSet = builder.getNewDataSet();
        this.comparisonKeys = builder.getComparisonKeys();
        this.converter = builder.getDataSetConverter();
        this.manager = builder.getManager();
    }

    public CompareResult result() {
        this.cleanupDirectory(this.converter.getDir());
        final CompareResult compareResult = this.manager.exec(this);
        final IDataSetProducer producer;
        try {
            producer = new DataSetProducerAdapter(new DefaultDataSet(compareResult.toITable()));
            producer.setConsumer(this.converter);
            producer.produce();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
        return compareResult;
    }

    protected void cleanupDirectory(final File dir) {
        if (dir.exists()) {
            final Delete delete = new Delete();
            final Project project = new Project();
            project.setName("dbunit-cli");
            project.setBaseDir(new File("."));
            project.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));
            delete.setProject(project);
            delete.setDir(dir);
            delete.execute();
        }
    }

    public ComparableDataSet getOldDataSet() {
        return this.oldDataSet;
    }

    public ComparableDataSet getNewDataSet() {
        return this.newDataSet;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.comparisonKeys;
    }

    public IDataSetConverter getConverter() {
        return this.converter;
    }

    public interface Manager {

        default CompareResult exec(final DataSetCompare dataSetCompare) {
            final List<CompareDiff> results = new ArrayList<>();
            this.getStrategies().forEach(it -> results.addAll(it.apply(dataSetCompare)));
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
                final List<CompareDiff> results = new ArrayList<>();
                try {
                    final int oldTableCounts = it.getOldDataSet().getTableNames().length;
                    final int newTableCounts = it.getNewDataSet().getTableNames().length;
                    if (oldTableCounts != newTableCounts) {
                        results.add(CompareDiff.Type.TABLE_COUNT.of()
                                .setOldDefine(String.valueOf(oldTableCounts))
                                .setNewDefine(String.valueOf(newTableCounts))
                                .build());
                    }
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        default Function<DataSetCompare, List<CompareDiff>> searchAddTables() {
            return (it) -> {
                final List<CompareDiff> results = new ArrayList<>();
                try {
                    final Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    final Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    newTables.stream()
                            .filter(table -> !Predicates.in(oldTables).apply(table))
                            .collect(Collectors.toSet())
                            .forEach(name -> results.add(CompareDiff.Type.TABLE_ADD.of()
                                    .setTargetName(name)
                                    .setNewDefine(name)
                                    .build()));
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        default Function<DataSetCompare, List<CompareDiff>> searchDeleteTables() {
            return (it) -> {
                final List<CompareDiff> results = new ArrayList<>();
                try {
                    final Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    final Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    oldTables.stream()
                            .filter(table -> !Predicates.in(newTables).apply(table))
                            .collect(Collectors.toSet())
                            .forEach(name -> results.add(CompareDiff.Type.TABLE_DELETE.of()
                                    .setTargetName(name)
                                    .setOldDefine(name)
                                    .build()));
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        default Function<DataSetCompare, List<CompareDiff>> searchModifyTables() {
            return (it) -> {
                final List<CompareDiff> results = new ArrayList<>();
                try {
                    final Set<String> oldTables = Sets.newHashSet(it.getOldDataSet().getTableNames());
                    final Set<String> newTables = Sets.newHashSet(it.getNewDataSet().getTableNames());
                    Sets.intersection(oldTables, newTables).forEach(tableName -> {
                        final ComparableTable oldTable = it.getOldDataSet().getTable(tableName);
                        final ComparableTable newTable = it.getNewDataSet().getTable(tableName);
                        if (it.getComparisonKeys().hasAdditionalSetting(oldTable.getTableMetaData().getTableName())) {
                            results.addAll(this.compareTable(new TableCompare(oldTable, newTable, it.getComparisonKeys(), it.getConverter())));
                        }
                    });
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
                return results;
            };
        }

        List<CompareDiff> compareTable(TableCompare tableCompare);

        CompareResult toCompareResult(ComparableDataSet oldDataSet, ComparableDataSet newDataSet, List<CompareDiff> results);
    }

    static class TableCompare {
        private final ComparableTable oldTable;
        private final ComparableTable newTable;
        private final AddSettingColumns comparisonKeys;
        private final IDataSetConverter converter;
        private final int columnLength;
        private final List<String> keyColumns;

        public TableCompare(final ComparableTable oldTable, final ComparableTable newTable, final AddSettingColumns comparisonKeys, final IDataSetConverter converter) {
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
            return this.oldTable;
        }

        public ComparableTable getNewTable() {
            return this.newTable;
        }

        public int getColumnLength() {
            return this.columnLength;
        }

        public List<String> getKeyColumns() {
            return this.keyColumns;
        }

        public AddSettingColumns getComparisonKeys() {
            return this.comparisonKeys;
        }

        public IDataSetConverter getConverter() {
            return this.converter;
        }

        protected int getColumnLength(final ComparableTable table) {
            try {
                return table.getTableMetaData().getColumns().length;
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        }

    }
}
