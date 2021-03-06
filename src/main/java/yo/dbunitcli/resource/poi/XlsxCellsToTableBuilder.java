package yo.dbunitcli.resource.poi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.poi.ss.util.CellReference;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XlsxCellsToTableBuilder {

    public static XlsxCellsToTableBuilder NO_TARGET = new XlsxCellsToTableBuilder(Lists.newArrayList()) {
        @Override
        public void handle(CellReference reference, String formattedValue) {
            // no handle
        }
    };
    private final String[] tableNames;

    private final Map<String, ITableMetaData> tableMetaDataMap = Maps.newHashMap();

    private final Map<String, List<XlsxCellsTableDefine>> columnDefine = Maps.newHashMap();

    private final Map<String, List<Object[]>> row = Maps.newHashMap();

    public XlsxCellsToTableBuilder(List<XlsxCellsTableDefine> tableDefines) {
        this.tableNames = new String[tableDefines.size()];
        for (int i = 0, j = tableNames.length; i < j; i++) {
            XlsxCellsTableDefine def = tableDefines.get(i);
            this.tableNames[i] = def.getTableName();
            this.tableMetaDataMap.put(def.getTableName(), def.getTableMetaData());
            for (String cellAddress : def.getTargetAddresses()) {
                if (!this.columnDefine.containsKey(cellAddress)) {
                    this.columnDefine.put(cellAddress, Lists.newArrayList());
                }
                this.columnDefine.get(cellAddress).add(def);
            }
            this.row.put(def.getTableName(), new ArrayList<>());
            List<Object[]> targetRows = this.row.get(def.getTableName());
            for (int rowIndex = 0, rowCount = def.rowCount(); rowIndex < rowCount; rowIndex++) {
                targetRows.add(rowIndex, new Object[def.columnCount()]);
            }
        }
    }

    public void handle(CellReference reference, String formattedValue) {
        String ref = reference.formatAsString()
                .replaceAll(".+!", "")
                .replaceAll("\\$", "");
        try {
            if (this.columnDefine.containsKey(ref)) {
                for (XlsxCellsTableDefine it : this.columnDefine.get(ref)) {
                    ITableMetaData metaData = tableMetaDataMap.get(it.getTableName());
                    this.row.get(it.getTableName())
                            .get(it.getRowIndex(ref))[metaData.getColumnIndex(it.getColumnName(ref))] = formattedValue;
                }
            }
        } catch (DataSetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String[] getTableNames() {
        return this.tableNames;
    }

    public ITableMetaData getTableMetaData(String tableName) {
        return this.tableMetaDataMap.get(tableName);
    }

    public List<Object[]> getRows(String tableName) {
        return row.get(tableName);
    }
}
