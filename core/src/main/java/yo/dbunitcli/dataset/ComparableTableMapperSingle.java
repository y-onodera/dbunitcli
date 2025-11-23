package yo.dbunitcli.dataset;

import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.dataset.DataSetException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComparableTableMapperSingle implements ComparableTableMapper {

    private final AddSettingTableMetaData baseMetaData;
    private final List<Integer> filteredRowIndexes;
    private final Collection<Object[]> rows;
    private final TableSplitter splitter;
    private final List<String> currentKeys = new ArrayList<>();
    private AddSettingTableMetaData metaData;
    private int no;
    private boolean startTable;
    private int addCount = 0;
    private int addRowCount = 0;
    private int breakKeyCount = 0;
    private IDataSetConverter converter;
    private Map<String, Integer> alreadyWrite;
    private List<ComparableTableJoin> joins;
    private List<ComparableTableMappingTask> chain;
    private boolean chainRun;

    public ComparableTableMapperSingle(final AddSettingTableMetaData metaData) {
        this.baseMetaData = metaData;
        this.splitter = metaData.getTableSplitter();
        this.metaData = this.splitter.getMetaData(this.baseMetaData, this.no);
        this.rows = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
    }

    @Override
    public void startTable(final IDataSetConverter converter, final Map<String, Integer> alreadyWrite, final List<ComparableTableJoin> joins, final List<ComparableTableMappingTask> chain, final boolean chainRun) {
        this.converter = converter;
        this.alreadyWrite = alreadyWrite;
        this.joins = joins.stream()
                .filter(it -> it.hasRelation(this.metaData.getTableName()))
                .collect(Collectors.toList());
        this.chain = chain;
        this.chainRun = chainRun;
        if (this.isEnableRowProcessing() && this.converter.isExportEmptyTable()) {
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
    public void endTable(final TreeMap<String, ComparableTable> orderedTableNameMap) {
        if (this.isEnableRowProcessing() && this.startTable) {
            this.alreadyWrite.put(this.metaData.getTableName(), this.getAddRowCount());
            if (!this.chain.isEmpty()) {
                final ComparableTableMappingTask next = this.chain.getFirst();
                final List<ComparableTableMappingTask> restChain = this.chain.size() <= 1
                        ? List.of()
                        : this.chain.subList(1, this.chain.size());
                next.run(new ComparableTableMappingContext(next.param().tableSeparators()
                        , this.converter
                        , orderedTableNameMap
                        , this.alreadyWrite
                        , this.joins
                        , restChain
                        , true
                ));
            } else {
                try {
                    this.converter.endTable();
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
            }
        } else {
            final String resultTableName = this.metaData.getTableName();
            if (orderedTableNameMap.containsKey(resultTableName)) {
                final ComparableTable other = orderedTableNameMap.get(resultTableName);
                final AddSettingTableMetaData.Rows add = other.getRows().add(this.metaData.distinct(this.rows, this.filteredRowIndexes));
                orderedTableNameMap.put(resultTableName, this.metaData.isNeedDistinct()
                        ? new ComparableTable.Builder(this.metaData).setRows(add.distinct()).build()
                        : new ComparableTable.Builder(this.metaData).setRows(add).build());
            } else {
                try {
                    if (this.splitter.isSplit()) {
                        this.addSplitResultTo(orderedTableNameMap);
                    } else {
                        orderedTableNameMap.put(resultTableName, this.lazyProcess());
                    }
                } catch (final AmbiguousTableNameException e) {
                    throw new AssertionError(e);
                }
            }
            this.joins.stream()
                    .filter(it -> it.getCondition().right().equals(resultTableName))
                    .forEach(it -> it.setRight(orderedTableNameMap.get(resultTableName)));
            this.joins.stream()
                    .filter(it -> it.getCondition().left().equals(resultTableName))
                    .forEach(it -> it.setLeft(orderedTableNameMap.get(resultTableName)));
            this.alreadyWrite.put(this.metaData.getTableName(), this.getAddRowCount());
        }
    }

    protected void addSplitResultTo(final TreeMap<String, ComparableTable> orderedTableNameMap) throws AmbiguousTableNameException {
        final AddSettingTableMetaData.Rows distinctRows = this.baseMetaData.distinct(this.rows, this.filteredRowIndexes);
        final ComparableTable sorted = new ComparableTable.Builder(this.baseMetaData).setRows(distinctRows).build();
        AddSettingTableMetaData splitMetaData = this.metaData;
        ArrayList<Integer> targetRowIndexes = new ArrayList<>();
        ArrayList<Object[]> targetValues = new ArrayList<>();
        final List<String> keys = new ArrayList<>();
        int keyCount = 0;
        int splitCount = 0;
        for (int i = 0, j = distinctRows.size(); i < j; i++) {
            final Object[] row = sorted.getRow(i);
            if (!this.splitter.breakKeys().isEmpty()) {
                final List<String> breakKey = this.metaData.getBreakKeys(row);
                if (i == 0) {
                    keys.addAll(breakKey);
                } else {
                    if (!keys.equals(breakKey)) {
                        keyCount++;
                        keys.clear();
                        keys.addAll(breakKey);
                    }
                }
            } else {
                keyCount++;
            }
            if (this.splitter.isLimit(keyCount)) {
                orderedTableNameMap.put(splitMetaData.getTableName()
                        , new ComparableTable.Builder(splitMetaData)
                                .setRows(new AddSettingTableMetaData.Rows(targetValues, targetRowIndexes))
                                .build());
                targetRowIndexes = new ArrayList<>();
                targetValues = new ArrayList<>();
                keyCount = 0;
                splitMetaData = this.splitter.getMetaData(this.baseMetaData, ++splitCount);
            }
            targetValues.add(row);
            if (this.metaData.hasRowFilter()) {
                targetRowIndexes.add(distinctRows.filteredRowIndexes().get(i));
            }
        }
        orderedTableNameMap.put(splitMetaData.getTableName()
                , new ComparableTable.Builder(splitMetaData)
                        .setRows(new AddSettingTableMetaData.Rows(targetValues, targetRowIndexes))
                        .build());
    }

    protected void processingStart() {
        if (this.chainRun) {
            return;
        }
        try {
            if (this.alreadyWrite.containsKey(this.metaData.getTableName())) {
                this.converter.reStartTable(this.metaData, this.alreadyWrite.get(this.metaData.getTableName()));
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
                if (this.isEnableRowProcessing()) {
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
        if (!this.splitter.breakKeys().isEmpty()) {
            final List<String> keys = this.metaData.getBreakKeys(applySetting);
            if (!this.currentKeys.equals(keys)) {
                this.breakKeyCount++;
                this.currentKeys.clear();
                this.currentKeys.addAll(keys);
            }
        }
        if (this.splitter.isLimit(this.getBreakKeyCount())) {
            this.endTable(null);
            this.no++;
            this.metaData = this.splitter.getMetaData(this.baseMetaData, this.no);
            this.processingStart();
            this.addRowCount = 0;
            this.breakKeyCount = 0;
        }
    }

    protected void add(final ComparableTable other) {
        IntStream.range(0, other.getRowCount()).forEach(rowNum ->
                this.addValue(Arrays.stream(this.metaData.getColumns())
                        .map(column -> other.getValue(rowNum, column.getColumnName()))
                        .toArray(Object[]::new)));
    }

    protected ComparableTable lazyProcess() {
        return new ComparableTable.Builder(this.metaData)
                .setRows(this.metaData.distinct(this.rows, this.filteredRowIndexes))
                .build();
    }

    protected boolean isEnableRowProcessing() {
        return this.converter != null
                && this.metaData.getOrderColumns().length == 0
                && !this.metaData.isNeedDistinct()
                && this.joins.isEmpty();
    }

    protected int getAddRowCount() {
        return this.addRowCount + Optional.ofNullable(this.alreadyWrite.get(this.metaData.getTableName())).orElse(0);
    }

    protected int getBreakKeyCount() {
        return this.splitter.breakKeys().isEmpty() ? this.getAddRowCount() : this.breakKeyCount;
    }
}
