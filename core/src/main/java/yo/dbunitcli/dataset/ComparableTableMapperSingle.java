package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComparableTableMapperSingle implements ComparableTableMapper {

    private final IDataSetConverter converter;
    private final WrittenTables alreadyWrite;
    private final TreeMap<String, ComparableTable> contextShareTableMap;
    /**
     * このテーブルに関係するjoin
     */
    private final List<ComparableTableJoin> joins;
    /**
     * 読込（コンテキスト）のすべてのjoin。続くソースもこのjoinに関係させる
     */
    private final List<ComparableTableJoin> contextJoins;
    private final List<ComparableTableMappingTask> chain;
    private final boolean chainRun;
    private final boolean enableRowProcessing;
    private final TableSeparators tableSeparators;
    private final AddSettingTableMetaData baseMetaData;
    private final List<Integer> filteredRowIndexes;
    private final Collection<Object[]> rows;
    private final TableSplitter splitter;
    private final WrittenTables.SplitState splitState;
    private final boolean hasOutput;
    private final ComparableTableMappingContext.Listener listener;
    private AddSettingTableMetaData metaData;
    private boolean startTable;
    private int addCount = 0;
    private int addRowCount = 0;

    public ComparableTableMapperSingle(final AddSettingTableMetaData metaData, final IDataSetConverter converter,
                                       final TreeMap<String, ComparableTable> contextShareTableMap,
                                       final WrittenTables alreadyWrite, final List<ComparableTableJoin> joins,
                                       final List<ComparableTableMappingTask> chain, final boolean chainRun,
                                       final boolean enableRowProcessing, final TableSeparators tableSeparators,
                                       final ComparableTableMappingContext.Listener listener) {
        this.baseMetaData = metaData;
        this.splitter = metaData.getTableSplitter();
        // 行を流しながら分割する場合は、同じテーブルに前のソースが書いた続きから分割する
        this.splitState = enableRowProcessing && this.splitter.isSplit()
                ? alreadyWrite.split(this.splitter.getMetaData(metaData, 0).getTableName(), this.splitter)
                : WrittenTables.SplitState.start(this.splitter);
        this.metaData = this.splitter.getMetaData(this.baseMetaData, this.splitState.no());
        this.rows = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
        this.converter = converter;
        this.contextShareTableMap = contextShareTableMap;
        this.alreadyWrite = alreadyWrite;
        this.joins = joins.stream().filter(join -> join.hasRelation(metaData)).collect(Collectors.toList());
        this.contextJoins = joins;
        this.chain = chain;
        this.chainRun = chainRun;
        this.enableRowProcessing = enableRowProcessing;
        this.tableSeparators = tableSeparators;
        this.listener = listener;
        this.hasOutput = converter == null || converter.isRowRequired(metaData, this.joins);
    }

    @Override
    public void startTable() {
        if (this.enableRowProcessing && this.converter.isExportEmptyTable()) {
            this.processingStart();
            this.startTable = true;
        }
    }

    @Override
    public void addRow(final Object[] values) {
        if (this.metaData.isNeedDistinct()) {
            this.rows.add(values);
            this.addCount++;
        } else {
            this.addValue(this.metaData.applySetting(values));
        }
    }

    @Override
    public boolean hasOutput() {
        return this.hasOutput;
    }

    @Override
    public void endTable() {
        this.alreadyWrite.put(this.metaData.getTableName(), this.getAddRowCount());
        if (this.enableRowProcessing && this.splitter.isSplit()) {
            this.putSplitTable(this.metaData.getTableName());
        }
        if (this.enableRowProcessing && this.startTable) {
            if (!this.chain.isEmpty()) {
                final ComparableTableMappingTask next = this.chain.getFirst();
                final List<ComparableTableMappingTask> restChain = this.chain.size() == 1
                        ? List.of()
                        : this.chain.subList(1, this.chain.size());
                next.run(new ComparableTableMappingContext(this.chainSeparators(next)
                        , this.converter
                        , this.contextShareTableMap
                        , this.alreadyWrite
                        , this.contextJoins
                        , restChain
                        , true
                        , this.listener
                ));
            } else {
                try {
                    this.converter.endTable();
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
            }
        } else {
            if (this.splitter.isSplit()) {
                this.addSplitResult();
            } else if (this.contextShareTableMap.containsKey(this.metaData.getTableName())) {
                final ComparableTable other = this.contextShareTableMap.get(this.metaData.getTableName());
                final AddSettingTableMetaData.Rows add = other.getRows().add(this.metaData.distinct(this.rows,
                                                                                                    this.filteredRowIndexes));
                this.hold(this.metaData.getTableName(), this.metaData.isNeedDistinct()
                        ? new ComparableTable.Builder(this.metaData).setRows(add.distinct()).build()
                        : new ComparableTable.Builder(this.metaData).setRows(add).build());
            } else {
                this.hold(this.metaData.getTableName(), this.lazyProcess());
            }
            // 分割・既存のテーブルへの追加でも、続くソースを読む
            if (!this.chain.isEmpty()) {
                final ComparableTableMappingTask next = this.chain.getFirst();
                final List<ComparableTableMappingTask> restChain = this.chain.size() == 1
                        ? List.of()
                        : this.chain.subList(1, this.chain.size());
                next.run(new ComparableTableMappingContext(this.chainSeparators(next)
                        , null
                        , this.contextShareTableMap
                        , this.alreadyWrite
                        , this.contextJoins
                        , restChain
                        , true
                        , this.listener
                ));
            }
            this.joins.forEach(it -> it.setIfRelated(this.contextShareTableMap.get(this.metaData.getTableName())));
        }
    }

    private TableSeparators chainSeparators(final ComparableTableMappingTask task) {
        final TableSeparators base = task.param().tableSeparators();
        return base.then(this.tableSeparators.next());
    }

    /**
     * 保持した行を並べ替えて分割する。前のソースで同じテーブルを分割していれば、その行とまとめてから分割し直す
     * （行を流しながら分割する場合と同じく、ソースをまたいで分割する）
     */
    protected void addSplitResult() {
        final AddSettingTableMetaData.Rows distinctRows = this.mergePreviousSplit(this.baseMetaData.distinct(this.rows,
                                                                                                             this.filteredRowIndexes));
        final ComparableTable sorted = new ComparableTable.Builder(this.baseMetaData).setRows(distinctRows).build();
        AddSettingTableMetaData splitMetaData = this.splitter.getMetaData(this.baseMetaData, 0);
        ArrayList<Integer> targetRowIndexes = new ArrayList<>();
        ArrayList<Object[]> targetValues = new ArrayList<>();
        final TableSplitter.Counter counter = this.splitter.counter();
        int splitCount = 0;
        for (int i = 0, j = distinctRows.size(); i < j; i++) {
            final Object[] row = sorted.getRow(i);
            if (counter.isBreak(this.breakKeys(row), targetValues.size())) {
                this.hold(splitMetaData.getTableName()
                        , new ComparableTable.Builder(splitMetaData)
                                                      .setRows(new AddSettingTableMetaData.Rows(targetValues,
                                                                                                targetRowIndexes))
                                                      .build());
                this.putSplitTable(splitMetaData.getTableName());
                targetRowIndexes = new ArrayList<>();
                targetValues = new ArrayList<>();
                splitMetaData = this.splitter.getMetaData(this.baseMetaData, ++splitCount);
            }
            targetValues.add(row);
            if (this.metaData.hasRowFilter()) {
                targetRowIndexes.add(distinctRows.filteredRowIndexes().get(i));
            }
        }
        this.hold(splitMetaData.getTableName()
                , new ComparableTable.Builder(splitMetaData)
                                              .setRows(new AddSettingTableMetaData.Rows(targetValues, targetRowIndexes))
                                              .build());
        this.putSplitTable(splitMetaData.getTableName());
    }

    private void hold(final String tableName, final ComparableTable table) {
        this.contextShareTableMap.put(tableName, table);
        this.listener.held(tableName);
    }

    private void putSplitTable(final String tableName) {
        this.alreadyWrite.putSplitTable(tableName, this.splitter.tableName(this.baseMetaData.getTableName(), 0));
    }

    private AddSettingTableMetaData.Rows mergePreviousSplit(final AddSettingTableMetaData.Rows rows) {
        final List<Object[]> mergedRows = new ArrayList<>();
        final List<Integer> mergedIndexes = new ArrayList<>();
        ComparableTable table;
        for (int no = 0; (table = this.contextShareTableMap.remove(this.splitter.tableName(this.baseMetaData.getTableName(), no))) != null; no++) {
            mergedRows.addAll(table.getRows().rows());
            mergedIndexes.addAll(table.getRows().filteredRowIndexes());
        }
        if (mergedRows.isEmpty()) {
            return rows;
        }
        mergedRows.addAll(rows.rows());
        mergedIndexes.addAll(rows.filteredRowIndexes());
        final AddSettingTableMetaData.Rows merged = new AddSettingTableMetaData.Rows(mergedRows, mergedIndexes);
        return this.baseMetaData.isNeedDistinct() ? merged.distinct() : merged;
    }

    protected void processingStart() {
        if (this.chainRun) {
            return;
        }
        try {
            if (this.alreadyWrite.contains(this.metaData.getTableName())) {
                this.converter.reStartTable(this.metaData, this.alreadyWrite.rows(this.metaData.getTableName()));
            } else {
                this.converter.startTable(this.metaData);
            }
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void addValue(final Object[] applySetting) {
        try {
            if (applySetting != null) {
                if (this.enableRowProcessing) {
                    if (!this.converter.isExportEmptyTable() && !this.startTable) {
                        this.processingStart();
                        this.startTable = true;
                    }
                    this.splitTable(applySetting);
                    this.converter.row(applySetting);
                } else {
                    this.rows.add(applySetting);
                    if (this.metaData.hasRowFilter()) {
                        this.filteredRowIndexes.add(this.addCount);
                    }
                }
                this.addRowCount++;
            }
            this.addCount++;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected void splitTable(final Object[] applySetting) throws DataSetException {
        if (!this.splitter.isSplit()) {
            return;
        }
        if (this.splitState.counter().isBreak(this.breakKeys(applySetting), this.getAddRowCount())) {
            this.endTable();
            this.splitState.next();
            this.metaData = this.splitter.getMetaData(this.baseMetaData, this.splitState.no());
            this.processingStart();
            this.addRowCount = 0;
        }
    }

    private List<String> breakKeys(final Object[] row) {
        return this.splitter.breakKeys().isEmpty() ? List.of() : this.metaData.getBreakKeys(row);
    }

    protected void add(final ComparableTable other) {
        IntStream.range(0, other.getRowCount())
                 .forEach(rowNum -> this.addValue(
                         Arrays.stream(this.metaData.getColumns())
                               .map(column -> other.getValue(
                                       rowNum,
                                       column.getColumnName()))
                               .toArray(Object[]::new))
                 );
    }

    protected ComparableTable lazyProcess() {
        return new ComparableTable.Builder(this.metaData)
                .setRows(this.metaData.distinct(this.rows, this.filteredRowIndexes))
                .build();
    }

    protected int getAddRowCount() {
        return this.addRowCount + this.alreadyWrite.rows(this.metaData.getTableName());
    }

}
