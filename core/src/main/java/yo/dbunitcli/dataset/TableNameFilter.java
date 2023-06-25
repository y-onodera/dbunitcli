package yo.dbunitcli.dataset;


import java.util.Optional;
import java.util.regex.Pattern;

public record TableNameFilter(Pattern regInclude, Pattern regExclude) {

    public TableNameFilter(final String regInclude, final String regExclude) {
        this(Optional.ofNullable(regInclude).orElse("").isEmpty() ? null : Pattern.compile(regInclude)
                , Optional.ofNullable(regExclude).orElse("").isEmpty() ? null : Pattern.compile(regExclude)
        );
    }

    public boolean predicate(final String tableName) {
        return (this.regInclude == null || this.regInclude.matcher(tableName).find())
                && (this.regExclude == null || !this.regExclude.matcher(tableName).find());
    }
}
