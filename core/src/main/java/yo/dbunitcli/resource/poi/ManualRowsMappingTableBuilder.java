package yo.dbunitcli.resource.poi;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManualRowsMappingTableBuilder implements XlsxRowsToTableBuilder {

    public static ManualRowsMappingTableBuilder NO_TARGET = new ManualRowsMappingTableBuilder(Lists.newArrayList()) {
        @Override
        public void handle(CellReference reference, int currentCol, String formattedValue) {
            // no handle
        }
    };

    private final String[] tableNames;

    private final Map<String, Integer> tableStartRow = Maps.newHashMap();

    private final Map<String, ITableMetaData> tableMetaDataMap = Maps.newHashMap();

    private final Map<String, List<Integer>> targetIndex = Maps.newHashMap();

    private final Map<String, String[]> breakKey = Maps.newHashMap();

    private final List<Object> rowValues = Lists.newArrayList();
    private int currentTableIndex = -1;
    private ITableMetaData nowProcessing = null;

    public ManualRowsMappingTableBuilder(List<XlsxRowsTableDefine> tableDefines) {
        this.tableNames = new String[tableDefines.size()];
        for (int i = 0, j = tableDefines.size(); i < j; i++) {
            XlsxRowsTableDefine def = tableDefines.get(i);
            this.tableNames[i] = def.getTableName();
            this.tableStartRow.put(def.getTableName(), def.getDataStartRow());
            this.tableMetaDataMap.put(def.getTableName(), def.getTableMetaData());
            this.targetIndex.put(def.getTableName(), Lists.newArrayList(def.getCellIndexes()));
            this.breakKey.put(def.getTableName(), def.getBreakKey());
        }
    }

    @Override
    public boolean isTableStart(int rowNum) {
        return this.tableNames.length > this.currentTableIndex + 1
                && this.tableStartRow.get(this.tableNames[this.currentTableIndex + 1]) == rowNum + 1;
    }

    @Override
    public boolean hasRow(int rowNum) throws DataSetException {
        if (!this.isNowProcessing()) {
            return false;
        }
        ITableMetaData metaData = this.tableMetaDataMap.get(this.tableNames[this.currentTableIndex]);
        for (String conditionColumn : this.breakKey.get(this.tableNames[this.currentTableIndex])) {
            if (this.rowValues.size() <= metaData.getColumnIndex(conditionColumn)
                    || Strings.isNullOrEmpty(Optional.ofNullable(this.rowValues.get(metaData.getColumnIndex(conditionColumn)))
                    .orElse("").toString())) {
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
    public void handle(CellReference reference, int lastCol, String formattedValue) {
        if (!this.isNowProcessing()) {
            return;
        }
        int thisCol = reference.getCol();
        int missedCols = thisCol - lastCol - 1;
        for (int i = 0; i < missedCols; i++) {
            this.addValue(i + lastCol + 1, "");
        }
        this.addValue(thisCol, formattedValue);
    }

    @Override
    public String[] currentRow() throws DataSetException {
        if (this.nowProcessing.getColumns().length < rowValues.size()) {
            throw new AssertionError(rowValues + " large items than header:" + Arrays.toString(this.nowProcessing.getColumns()));
        } else if (rowValues.size() < this.nowProcessing.getColumns().length) {
            for (int i = rowValues.size(), j = this.nowProcessing.getColumns().length; i < j; i++) {
                rowValues.add("");
            }
        }
        return this.rowValues.toArray(new String[0]);
    }

    @Override
    public boolean isNowProcessing() {
        return this.nowProcessing != null;
    }

    protected void addValue(int i, String o) {
        if (this.targetIndex.get(this.tableNames[this.currentTableIndex]).contains(i)) {
            this.rowValues.add(o);
        }
    }
}
