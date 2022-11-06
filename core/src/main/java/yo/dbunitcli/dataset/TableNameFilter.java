package yo.dbunitcli.dataset;

import com.google.common.base.Strings;

import java.util.regex.Pattern;

public class TableNameFilter {
    private final String regInclude;
    private final String regExclude;
    private Pattern includePattern;
    private Pattern excludePattern;

    public TableNameFilter(final String regInclude, final String regExclude) {
        this.regInclude = regInclude;
        this.regExclude = regExclude;
        if (!Strings.isNullOrEmpty(this.regInclude)) {
            this.includePattern = Pattern.compile(this.regInclude);
        }
        if (!Strings.isNullOrEmpty(this.regExclude)) {
            this.excludePattern = Pattern.compile(this.regExclude);
        }
    }

    public boolean predicate(final String tableName) {
        return (this.includePattern == null || this.includePattern.matcher(tableName).find())
                && (this.excludePattern == null || !this.excludePattern.matcher(tableName).find());
    }
}
