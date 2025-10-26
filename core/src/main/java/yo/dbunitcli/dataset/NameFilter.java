package yo.dbunitcli.dataset;


import java.util.Optional;
import java.util.regex.Pattern;

public record NameFilter(Pattern regInclude, Pattern regExclude) {

    public NameFilter(final String regInclude, final String regExclude) {
        this(Optional.ofNullable(regInclude).orElse("").isEmpty() ? null : Pattern.compile(regInclude)
                , Optional.ofNullable(regExclude).orElse("").isEmpty() ? null : Pattern.compile(regExclude)
        );
    }

    public boolean predicate(final String tableName) {
        return (this.regInclude == null || this.regInclude.matcher(tableName).find())
                && (this.regExclude == null || !this.regExclude.matcher(tableName).find());
    }

    public String include() {
        return this.regInclude == null ? null : this.regInclude.pattern();
    }

    public String exclude() {
        return this.regExclude == null ? null : this.regExclude.pattern();
    }
}
