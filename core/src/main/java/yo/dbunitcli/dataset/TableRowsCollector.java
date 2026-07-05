package yo.dbunitcli.dataset;

import org.dbunit.dataset.ITableMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class TableRowsCollector implements IDataSetConverter {

    private final String targetTableName;
    private final List<Map<String, Object>> rows;
    private AddSettingTableMetaData currentMetaData;

    TableRowsCollector(final String targetTableName) {
        this(targetTableName, new ArrayList<>());
    }

    private TableRowsCollector(final String targetTableName, final List<Map<String, Object>> rows) {
        this.targetTableName = targetTableName;
        this.rows = rows;
    }

    List<Map<String, Object>> getRows() {
        return this.rows;
    }

    @Override
    public boolean isExportEmptyTable() {
        return false;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData metaData, final Integer writeRows) {
        this.currentMetaData = metaData;
    }

    @Override
    public void startTable(final ITableMetaData metaData) {
        this.currentMetaData = (AddSettingTableMetaData) metaData;
    }

    @Override
    public void endTable() {
        this.currentMetaData = null;
    }

    @Override
    public void row(final Object[] values) {
        if (this.currentMetaData == null || !this.currentMetaData.getTableName().equals(this.targetTableName)) {
            return;
        }
        this.rows.add(this.currentMetaData.rowToMap(values));
    }

    @Override
    public IDataSetConverter split() {
        return new TableRowsCollector(this.targetTableName, this.rows);
    }
}
