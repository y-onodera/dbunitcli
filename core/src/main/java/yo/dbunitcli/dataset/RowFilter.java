package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RowFilter {

    public static final RowFilter NONE = new RowFilter((it) -> Boolean.TRUE);

    private final Predicate<Map<String, Object>> filter;

    private final Function<String, String> tableRenameFunction;

    public RowFilter(final String expression) {
        this(createFilter(expression));
    }

    public RowFilter(final List<String> expressions) {
        this(createFilter(expressions));
    }

    public RowFilter(final Predicate<Map<String, Object>> filter) {
        this(filter, Function.identity());
    }

    public RowFilter(final Predicate<Map<String, Object>> filter, final Function<String, String> tableRenameFunction) {
        this.filter = filter;
        this.tableRenameFunction = tableRenameFunction;
    }

    public boolean test(final Map<String, Object> rowToMap) {
        return this.filter.test(rowToMap);
    }

    public RowFilter addFilter(final String expression) {
        return new RowFilter(this.filter.and(createFilter(expression)), this.tableRenameFunction);
    }

    public RowFilter add(final RowFilter other
            , final BiFunction<Function<String, String>, Function<String, String>, Function<String, String>> tableNameFunctionCompose) {
        return new RowFilter(this.filter.and(other.filter), tableNameFunctionCompose.apply(this.tableRenameFunction, other.tableRenameFunction));
    }

    static Predicate<Map<String, Object>> createFilter(final String expression) {
        final List<String> expressions = new ArrayList<>();
        expressions.add(expression);
        return createFilter(expressions);
    }

    static Predicate<Map<String, Object>> createFilter(final List<String> expressions) {
        final JexlEngine jexl = new JexlBuilder().create();
        final List<JexlExpression> expr = expressions
                .stream()
                .map(jexl::createExpression)
                .collect(Collectors.toList());
        return (map) -> expr.stream().allMatch(it -> Boolean.parseBoolean(it.evaluate(new MapContext(map)).toString()));
    }

    public String rename(final String tableName) {
        return this.tableRenameFunction.apply(tableName);
    }

    public RowFilter withRenameFunction(final Function<String, String> renameFunction) {
        return new RowFilter(this.filter, renameFunction);
    }

    public RowFilter editRenameFunction(final Function<Function<String, String>, Function<String, String>> renameFunction) {
        return new RowFilter(this.filter, renameFunction.apply(this.tableRenameFunction));
    }
}
