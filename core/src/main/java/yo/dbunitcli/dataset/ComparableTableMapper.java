package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ComparableTableMapper {

    private final List<Object[]> values;
    private final AddSettingTableMetaData addSettingTableMetaData;
    private final Column[] orderColumns;
    private final List<AddSettingTableMetaData> settingChain = Lists.newArrayList();
    private final List<Integer> filteredRowIndexes;
    private boolean startTable;
    private int addCount = 0;
    private IDataSetConsumer consumer;

    public ComparableTableMapper(AddSettingTableMetaData metaData, Column[] orderColumns, List<AddSettingTableMetaData> settings) {
        this.values = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
        this.addSettingTableMetaData = metaData;
        this.orderColumns = orderColumns;
        this.settingChain.addAll(settings);
    }

    public void setConsumer(IDataSetConsumer consumer) throws DataSetException {
        this.consumer = consumer;
        if (this.isEnableRowProcessing() && this.consumer.isExportEmptyTable()) {
            this.consumer.startTable(this.addSettingTableMetaData);
            this.startTable = true;
        }
    }

    public ComparableTable endTable() throws DataSetException {
        if (this.isEnableRowProcessing() && this.startTable) {
            this.consumer.endTable();
            return null;
        }
        return new ComparableTable(this.addSettingTableMetaData, this.orderColumns, this.values, this.filteredRowIndexes);
    }

    public String getTargetTableName() {
        return this.addSettingTableMetaData.getTableName();
    }

    public void addRow(Object[] values) throws DataSetException {
        Object[] applySettings = values;
        for (AddSettingTableMetaData metaData : this.settingChain) {
            applySettings = metaData.applySetting(applySettings);
            if (applySettings == null) {
                break;
            }
        }
        if (applySettings != null) {
            this.add(applySettings);
        }
    }

    public void add(ComparableTable other) throws DataSetException {
        for (int rowNum = 0, total = other.getRowCount(); rowNum < total; rowNum++) {
            Column[] columns = this.addSettingTableMetaData.getColumns();
            Object[] row = new Object[columns.length];
            for (int i = 0, j = columns.length; i < j; i++) {
                row[i] = other.getValue(rowNum, columns[i].getColumnName());
            }
            this.addValue(row);
        }
    }

    public void add(Object[] row) throws DataSetException {
        this.addValue(this.addSettingTableMetaData.applySetting(row));
    }

    protected void addValue(Object[] applySetting) throws DataSetException {
        if (applySetting != null) {
            if (this.isEnableRowProcessing()) {
                if (!this.consumer.isExportEmptyTable() && !this.startTable) {
                    this.consumer.startTable(this.addSettingTableMetaData);
                    this.startTable = true;
                }
                this.consumer.row(applySetting);
            } else {
                this.values.add(applySetting);
                if (this.addSettingTableMetaData.hasRowFilter()) {
                    this.filteredRowIndexes.add(this.addCount);
                }
            }
        }
        this.addCount++;
    }

    private boolean isEnableRowProcessing() {
        return this.consumer != null && this.orderColumns.length == 0;
    }

}
