package yo.dbunitcli.application;

import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.TableMetaDataFilter;
import yo.dbunitcli.dataset.AddSettingTableMetaData;

public record SplitOrNameFilter(String name) implements TableMetaDataFilter {
    @Override
    public boolean test(ITableMetaData metaData) {
        if (metaData instanceof AddSettingTableMetaData meta) {
            if (meta.getTableSplitter().isSplit()) {
                return this.name.contains(meta.getTableName());
            }
        }
        return this.test(metaData.getTableName());
    }

    @Override
    public boolean test(String tableName) {
        return tableName.equals(this.name);
    }
}
