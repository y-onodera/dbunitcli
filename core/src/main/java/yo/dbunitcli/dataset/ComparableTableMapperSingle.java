package yo.dbunitcli.dataset;

import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.OrderedTableNameMap;

import java.util.*;
import java.util.stream.IntStream;

public class ComparableTableMapperSingle implements ComparableTableMapper {

    private final AddSettingTableMetaData baseMetaData;
    private final Column[] orderColumns;
    private final List<Integer> filteredRowIndexes;
    private final Collection<Object[]> values;
    private final TableSplitter splitter;
    private AddSettingTableMetaData metaData;
    private int no;
    private boolean startTable;
    private int addCount = 0;
    private int addRowCount = 0;
    private final List<String> currentKeys = new ArrayList<>();
    private int breakKeyCount = 0;
    private IDataSetConverter converter;
    private Map<String, Integer> alreadyWrite;

    public ComparableTableMapperSingle(final AddSettingTableMetaData metaData, final Column[] orderColumns) {
        this.splitter = metaData.getTableSplitter();
        this.baseMetaData = metaData;
        this.metaData = this.splitter.getMetaData(this.baseMetaData, this.no);
        this.orderColumns = orderColumns;
        this.values = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
    }

    @Override
    public void startTable(final IDataSetConverter converter, final Map<String, Integer> alreadyWrite) {
        this.converter = converter;
        this.alreadyWrite = alreadyWrite;
        if (this.isEnableRowProcessing() && this.converter.isExportEmptyTable()) {
            this.processingStart();
            this.startTable = true;
        }
    }

    @Override
    public void addRow(final Object[] values) {
        this.addValue(this.metaData.applySetting(values));
    }

    @Override
    public void endTable(final OrderedTableNameMap orderedTableNameMap) {
        if (this.isEnableRowProcessing() && this.startTable) {
            try {
                this.converter.endTable();
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        } else {
            final String resultTableName = this.metaData.getTableName();
            if (orderedTableNameMap.containsTable(resultTableName)) {
                final ComparableTable existingTable = (ComparableTable) orderedTableNameMap.get(resultTableName);
                this.add(existingTable);
                orderedTableNameMap.update(resultTableName, this.getResult());
            } else {
                try {
                    if (this.splitter.isSplit()) {
                        final ComparableTable sorted = new ComparableTable(this.baseMetaData, this.orderColumns, this.values, this.filteredRowIndexes);
                        AddSettingTableMetaData splitMetaData = this.metaData;
                        ArrayList<Integer> targetRowIndexes = new ArrayList<>();
                        ArrayList<Object[]> targetValues = new ArrayList<>();
                        final List<String> keys = new ArrayList<>();
                        int keyCount = 0;
                        int splitCount = 0;
                        for (int i = 0, j = this.values.size(); i < j; i++) {
                            final Object[] row = sorted.getRow(i);
                            if (this.splitter.breakKeys().size() > 0) {
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
                                orderedTableNameMap.add(splitMetaData.getTableName(), new ComparableTable(splitMetaData, this.orderColumns, targetValues, targetRowIndexes));
                                targetRowIndexes = new ArrayList<>();
                                targetValues = new ArrayList<>();
                                keyCount = 0;
                                splitMetaData = this.splitter.getMetaData(this.baseMetaData, ++splitCount);
                            }
                            targetValues.add(row);
                            if (this.metaData.hasRowFilter()) {
                                targetRowIndexes.add(this.filteredRowIndexes.get(i));
                            }
                        }
                        orderedTableNameMap.add(splitMetaData.getTableName(), new ComparableTable(splitMetaData, this.orderColumns, targetValues, targetRowIndexes));
                    } else {
                        orderedTableNameMap.add(resultTableName, this.getResult());
                    }
                } catch (final AmbiguousTableNameException e) {
                    throw new AssertionError(e);
                }
            }
        }
        this.alreadyWrite.put(this.metaData.getTableName(), this.getAddRowCount());
    }

    protected void processingStart() {
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
                        this.converter.startTable(this.metaData);
                        this.startTable = true;
                    }
                    this.splitTable(applySetting);
                    this.converter.row(applySetting);
                } else {
                    this.values.add(applySetting);
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
        if (this.splitter.breakKeys().size() > 0) {
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
            this.converter.startTable(this.metaData);
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

    protected ComparableTable getResult() {
        return new ComparableTable(this.metaData, this.orderColumns, this.values, this.filteredRowIndexes);
    }

    protected boolean isEnableRowProcessing() {
        return this.converter != null && this.orderColumns.length == 0;
    }

    protected int getAddRowCount() {
        return this.addRowCount + Optional.ofNullable(this.alreadyWrite.get(this.metaData.getTableName())).orElse(0);
    }

    protected int getBreakKeyCount() {
        return this.splitter.breakKeys().size() == 0 ? this.getAddRowCount() : this.breakKeyCount;
    }
}
