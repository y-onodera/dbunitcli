package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;

import java.util.List;

public class ComparableTableMapper {
    private final ComparableTable result;
    private final List<AddSettingTableMetaData> settingChain = Lists.newArrayList();

    public ComparableTableMapper(ComparableTable result, List<AddSettingTableMetaData> settingChain) {
        this.result = result;
        this.settingChain.addAll(settingChain);
    }

    public ComparableTable result() {
        return this.result;
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
            this.result.addRow(applySettings);
        }
    }
}
