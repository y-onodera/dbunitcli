package yo.dbunitcli.common.filter;

import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.dataset.TableMetaDataWithSource;

import java.util.regex.Pattern;

public record WithFilePathMatchFilter(SourceFilter base, Pattern filePath) implements SourceFilter {
    public WithFilePathMatchFilter(final SourceFilter base, final String filePath) {
        this(base, Pattern.compile(filePath));
    }

    @Override
    public boolean test(final ITableMetaData originMetaData) {
        if (originMetaData instanceof final TableMetaDataWithSource withSource) {
            if (!this.filePath().matcher(withSource.source().filePath()).matches()) {
                return false;
            }
        } else {
            return false;
        }
        return this.base.test(originMetaData);
    }

    @Override
    public boolean test(final String tableName) {
        return this.base.test(tableName);
    }
}
