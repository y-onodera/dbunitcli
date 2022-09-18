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
    private int addRowCount = 0;
    private IDataSetWriter consumer;

    public ComparableTableMapper(AddSettingTableMetaData metaData, Column[] orderColumns, List<AddSettingTableMetaData> settings) {
        this.values = new ArrayList<>();
        this.filteredRowIndexes = new ArrayList<>();
        this.addSettingTableMetaData = metaData;
        this.orderColumns = orderColumns;
        this.settingChain.addAll(settings);
    }

    public void setConsumer(IDataSetWriter consumer) {
        this.consumer = consumer;
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
            this.addValue(row);
        }
    }

    public void add(Object[] row) {
        this.addValue(this.addSettingTableMetaData.applySetting(row));
    }

    protected void addValue(Object[] applySetting) {
        if (applySetting != null) {
            this.values.add(applySetting);
            if (this.addSettingTableMetaData.hasRowFilter()) {
                this.filteredRowIndexes.add(this.addRowCount);
            }
        }
        this.addRowCount++;
    }

}
