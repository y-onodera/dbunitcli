package yo.dbunitcli.common.filter;

import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.common.TableMetaDataFilter;
import yo.dbunitcli.common.TableMetaDataWithSource;

import java.util.regex.Pattern;

public record WithFilePathMatchFilter(TableMetaDataFilter base, Pattern filePath) implements TableMetaDataFilter {
    public WithFilePathMatchFilter(final TableMetaDataFilter base, final String filePath) {
        this(base, Pattern.compile(filePath));
    }

    @Override
    public boolean test(final ITableMetaData metaData) {
        if (metaData instanceof final TableMetaDataWithSource withSource) {
            if (!this.filePath().matcher(withSource.source().filePath()).matches()) {
                return false;
            }
        } else {
            return false;
        }
        return this.base.test(metaData);
    }

    @Override
    public boolean test(final String tableName) {
        return this.base.test(tableName);
    }
}
