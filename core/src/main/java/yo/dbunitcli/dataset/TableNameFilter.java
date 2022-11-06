package yo.dbunitcli.dataset;

import com.google.common.base.Strings;

import java.util.regex.Pattern;

public class TableNameFilter {
    private Pattern includePattern;
    private Pattern excludePattern;

    public TableNameFilter(final String regInclude, final String regExclude) {
        if (!Strings.isNullOrEmpty(regInclude)) {
            this.includePattern = Pattern.compile(regInclude);
        }
        if (!Strings.isNullOrEmpty(regExclude)) {
            this.excludePattern = Pattern.compile(regExclude);
        }
    }

    public boolean predicate(final String tableName) {
        return (this.includePattern == null || this.includePattern.matcher(tableName).find())
                && (this.excludePattern == null || !this.excludePattern.matcher(tableName).find());
    }
}
