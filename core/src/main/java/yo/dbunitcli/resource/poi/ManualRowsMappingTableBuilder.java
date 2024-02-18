package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;
import java.util.stream.IntStream;

public class ManualRowsMappingTableBuilder implements XlsxRowsToTableBuilder {

    public static ManualRowsMappingTableBuilder NO_TARGET = new ManualRowsMappingTableBuilder(new ArrayList<>(), XlsxSchema.FileInfo.NONE) {
        @Override
        public void handle(final CellReference reference, final int currentCol, final String formattedValue) {
            // no handle
        }
    };

    private final String[] tableNames;

    private final Map<String, Integer> tableStartRow = new HashMap<>();

    private final Map<String, ITableMetaData> tableMetaDataMap = new HashMap<>();

    private final Map<String, List<Integer>> targetIndex = new HashMap<>();

    private final Map<String, String[]> breakKey = new HashMap<>();

    private final List<String> rowValues = new ArrayList<>();
    private int currentTableIndex = -1;
    private ITableMetaData nowProcessing = null;

    public ManualRowsMappingTableBuilder(final List<XlsxRowsTableDefine> tableDefines, final XlsxSchema.FileInfo fileInfo) {
        this.tableNames = new String[tableDefines.size()];
        IntStream.range(0, tableDefines.size()).forEach(i -> {
            final XlsxRowsTableDefine def = tableDefines.get(i);
            this.tableNames[i] = def.tableName();
            this.tableStartRow.put(def.tableName(), def.dataStartRow());
            this.tableMetaDataMap.put(def.tableName()
                    , def.addOptional() ? fileInfo.wrap(def.tableMetaData()) : def.tableMetaData());
            this.targetIndex.put(def.tableName(), Arrays.asList(def.cellIndexes()));
            this.breakKey.put(def.tableName(), def.breakKey());
        });
    }

    @Override
    public boolean isTableStart(final int rowNum) {
        return this.tableNames.length > this.currentTableIndex + 1
                && this.tableStartRow.get(this.tableNames[this.currentTableIndex + 1]) == rowNum + 1;
    }

    @Override
    public boolean hasRow(final int rowNum) {
        if (!this.isNowProcessing()) {
            return false;
        }
        final ITableMetaData metaData = this.tableMetaDataMap.get(this.tableNames[this.currentTableIndex]);
        for (final String conditionColumn : this.breakKey.get(this.tableNames[this.currentTableIndex])) {
            if (this.rowValues.size() <= this.getColumnIndex(metaData, conditionColumn)
                    || Optional.ofNullable(this.rowValues.get(this.getColumnIndex(metaData, conditionColumn)))
                    .orElse("").isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ITableMetaData startNewTable() {
        this.nowProcessing = this.tableMetaDataMap.get(this.tableNames[++this.currentTableIndex]);
        return this.nowProcessing;
    }

    @Override
    public void clearRowValue() {
        this.rowValues.clear();
    }

    @Override
    public void handle(final CellReference reference, final int lastCol, final String formattedValue) {
        if (!this.isNowProcessing()) {
            return;
        }
        final int thisCol = reference.getCol();
        final int missedCols = thisCol - lastCol - 1;
        IntStream.range(0, missedCols).forEach(i -> this.addValue(i + lastCol + 1, ""));
        this.addValue(thisCol, formattedValue);
    }

    @Override
    public String[] currentRow() {
        if (this.getColumnLength() < this.rowValues.size()) {
            throw new AssertionError(this.rowValues + " large items than header:" + Arrays.toString(this.getNowProcessingColumns()));
        } else if (this.rowValues.size() < this.getColumnLength()) {
            IntStream.range(this.rowValues.size(), this.getColumnLength())
                    .forEach(i -> this.rowValues.add(""));
        }
        if (this.nowProcessing instanceof XlsxSchema.FileInfo.AddFileInfoMetaData option) {
            option.setValueTo(this.rowValues);
        }
        return this.rowValues.toArray(new String[0]);
    }

    @Override
    public boolean isNowProcessing() {
        return this.nowProcessing != null;
    }

    protected void addValue(final int i, final String o) {
        if (this.targetIndex.get(this.tableNames[this.currentTableIndex]).contains(i)) {
            this.rowValues.add(o);
        }
    }

    private int getColumnLength() {
        return this.getNowProcessingColumns().length;
    }

    private Column[] getNowProcessingColumns() {
        try {
            return this.nowProcessing.getColumns();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private int getColumnIndex(final ITableMetaData metaData, final String conditionColumn) {
        try {
            return metaData.getColumnIndex(conditionColumn);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
