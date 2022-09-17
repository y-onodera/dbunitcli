package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;

import java.util.ArrayList;
import java.util.List;

public class ComparableTableMapper {

    private final List<Object[]> values;
    private final AddSettingTableMetaData addSettingTableMetaData;
    private final Column[] orderColumns;
    private final List<AddSettingTableMetaData> settingChain = Lists.newArrayList();
    private final List<Integer> filteredRowIndexes;

    public ComparableTableMapper(AddSettingTableMetaData metaData, Column[] orderColumns, List<AddSettingTableMetaData> settings) {
        this.values = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
        this.addSettingTableMetaData = metaData;
        this.orderColumns = orderColumns;
        this.settingChain.addAll(settings);
    }

    public ComparableTable result() {
        return new ComparableTable(this.addSettingTableMetaData, this.orderColumns, this.values, this.filteredRowIndexes);
    }

    public String getTargetTableName() {
        return this.addSettingTableMetaData.getTableName();
    }

    public void addRow(Object[] values) {
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
            this.add(row);
        }
    }

    public void add(Object[] row) {
        this.values.add(row);
        if (this.addSettingTableMetaData.hasRowFilter() && this.addSettingTableMetaData.applySetting(row) != null) {
            this.filteredRowIndexes.add(this.values.size() - 1);
        }
    }

}
