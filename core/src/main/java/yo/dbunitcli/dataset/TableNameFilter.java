package yo.dbunitcli.dataset;


import java.util.Optional;
import java.util.regex.Pattern;

public class TableNameFilter {
    private Pattern includePattern;
    private Pattern excludePattern;

    public TableNameFilter(final String regInclude, final String regExclude) {
        if (!Optional.ofNullable(regInclude).orElse("").isEmpty()) {
            this.includePattern = Pattern.compile(regInclude);
        }
        if (!Optional.ofNullable(regExclude).orElse("").isEmpty()) {
            this.excludePattern = Pattern.compile(regExclude);
        }
    }

    public boolean predicate(final String tableName) {
        return (this.includePattern == null || this.includePattern.matcher(tableName).find())
                && (this.excludePattern == null || !this.excludePattern.matcher(tableName).find());
    }
}
