package yo.dbunitcli.resource.poi;

import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.Source;
import yo.dbunitcli.dataset.TableMetaDataWithSource;

import java.util.*;
import java.util.stream.IntStream;

public class ManualRowsMappingTableBuilder implements XlsxRowsToTableBuilder {

    public static ManualRowsMappingTableBuilder NO_TARGET = new ManualRowsMappingTableBuilder(new ArrayList<>(), Source.NONE) {
        @Override
        public void handle(final CellReference reference, final int currentCol, final String formattedValue) {
            // no handle
        }
    };

    private final String[] tableNames;

    private final Map<String, Integer> tableStartRow = new HashMap<>();

    private final Map<String, TableMetaDataWithSource> tableMetaDataMap = new HashMap<>();

    private final Map<String, List<Integer>> targetIndex = new HashMap<>();

    private final Map<String, String[]> breakKey = new HashMap<>();

    private final List<String> rowValues = new ArrayList<>();
    private int currentTableIndex = -1;
    private TableMetaDataWithSource nowProcessing = null;

    public ManualRowsMappingTableBuilder(final List<XlsxRowsTableDefine> tableDefines, final Source source) {
        this.tableNames = new String[tableDefines.size()];
        IntStream.range(0, tableDefines.size()).forEach(i -> {
            final XlsxRowsTableDefine def = tableDefines.get(i);
            final Source sourceWithTableDefine = source.addFileInfo(def.addFileInfo() || source.addFileInfo());
            this.tableNames[i] = def.tableName();
            this.tableStartRow.put(def.tableName(), def.dataStartRow());
            this.tableMetaDataMap.put(def.tableName(), sourceWithTableDefine.wrap(def.tableMetaData()));
            this.targetIndex.put(def.tableName(), Arrays.asList(def.cellIndexes()));
            this.breakKey.put(def.tableName(), def.breakKey());
        });
    }

    @Override
    public boolean isTableStart(final int rowNum) {
        if (!this.existsToBeMapping()) {
            return false;
        }
        return this.getDataStartRowIndex() == rowNum;
    }

    @Override
    public boolean hasRow(final int rowNum) {
        if (!this.isNowProcessing()) {
            return false;
        }
        final String[] rows = this.currentRow();
        return Arrays.stream(this.breakKey.get(this.getTableName()))
                .anyMatch(it -> !Optional.ofNullable(rows[this.getColumnIndex(this.nowProcessing, it)])
                        .orElse("").isEmpty());
    }

    @Override
    public TableMetaDataWithSource startNewTable() {
        this.nowProcessing = this.tableMetaDataMap.get(this.tableNames[++this.currentTableIndex]);
        return this.nowProcessing;
    }

    @Override
    public void clearRowValue() {
        this.rowValues.clear();
    }

    @Override
    public void handle(final CellReference reference, final int lastCol, final String formattedValue) {
        if (!this.isNowProcessing() && !(this.existsToBeMapping() && reference.getRow() >= this.getDataStartRowIndex())) {
            return;
        }
        final int thisCol = reference.getCol();
        final int missedCols = thisCol - lastCol - 1;
        IntStream.range(0, missedCols).forEach(i -> this.addValue(i + lastCol + 1, ""));
        this.addValue(thisCol, formattedValue);
    }

    @Override
    public String[] currentRow() {
        if (this.nowProcessing.getColumnLength() < this.rowValues.size()) {
            throw new AssertionError(this.rowValues + " large items than header:" + Arrays.toString(this.nowProcessing.getColumns()));
        }
        return this.nowProcessing.withDefaultValuesToArray(this.rowValues);
    }

    @Override
    public boolean isNowProcessing() {
        return this.nowProcessing != null;
    }

    private void addValue(final int i, final String o) {
        if (this.targetIndex.get(this.getTableName()).contains(i)) {
            this.rowValues.add(o);
        }
    }

    private boolean existsToBeMapping() {
        return this.tableNames.length > this.currentTableIndex + 1;
    }

    private Integer getDataStartRowIndex() {
        return this.tableStartRow.get(this.getTableName());
    }

    private String getTableName() {
        if (this.isNowProcessing()) {
            return this.tableNames[this.currentTableIndex];
        }
        return this.tableNames[this.currentTableIndex + 1];
    }

    private int getColumnIndex(final ITableMetaData metaData, final String conditionColumn) {
        try {
            return metaData.getColumnIndex(conditionColumn);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }
}
