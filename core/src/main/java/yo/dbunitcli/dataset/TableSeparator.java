package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public record TableSeparator(TableSplitter splitter, Predicate<Map<String, Object>> filter) {

    public static final Predicate<Map<String, Object>> NO_FILTER = it -> Boolean.TRUE;
    public static final TableSeparator NONE = new TableSeparator(TableSplitter.NONE, NO_FILTER);

    public TableSeparator(final TableSplitter splitter, final String expression) {
        this(splitter, createFilter(expression));
    }

    public TableSeparator(final TableSplitter splitter, final List<String> expressions) {
        this(splitter, createFilter(expressions));
    }

    public boolean hasRowFilter() {
        return this.filter() != NO_FILTER;
    }

    public boolean test(final Map<String, Object> rowToMap) {
        return this.filter.test(rowToMap);
    }

    public TableSplitter getSplitter() {
        return this.splitter;
    }

    public String rename(final String tableName) {
        if (this.splitter.limit() > 0) {
            return tableName;
        }
        return this.splitter.renameFunction().apply(tableName, 0);
    }

    public TableSeparator addFilter(final List<String> expressions) {
        return this.with(createFilter(expressions));
    }

    public TableSeparator add(final TableSeparator other) {
        return this.with(other.filter()).with(other.splitter());
    }

    public TableSeparator with(final Predicate<Map<String, Object>> add) {
        if (add == NONE.filter()) {
            return this;
        } else if (this.filter == NONE.filter()) {
            return new TableSeparator(this.splitter, add);
        }
        return new TableSeparator(this.splitter, this.filter.and(add));
    }

    public TableSeparator with(final TableSplitter other) {
        if (this.splitter == TableSplitter.NONE) {
            return new TableSeparator(other, this.filter);
        } else if (other == TableSplitter.NONE || this.splitter.equals(other)) {
            return this;
        } else if (!other.isSplit() && !this.splitter.renameFunction().equals(other.renameFunction())) {
            return new TableSeparator(this.splitter.builder()
                    .setRenameFunction(this.splitter().renameFunction().compose(other.renameFunction()))
                    .build(), this.filter);
        } else if (!this.splitter.isSplit() && other.isSplit() && !this.splitter.renameFunction().equals(other.renameFunction())) {
            return new TableSeparator(other.builder()
                    .setRenameFunction(this.splitter().renameFunction().andThen(other.renameFunction()))
                    .build(), this.filter);
        }
        throw new UnsupportedOperationException("splitter define is conflict:" + this);
    }

    static Predicate<Map<String, Object>> createFilter(final String expression) {
        return createFilter(Collections.singletonList(expression));
    }

    static Predicate<Map<String, Object>> createFilter(final List<String> expressions) {
        final JexlEngine jexl = new JexlBuilder().create();
        final List<JexlExpression> expr = expressions
                .stream()
                .filter(it -> !it.trim().isEmpty())
                .map(jexl::createExpression)
                .toList();
        if (expr.size() == 0) {
            return NONE.filter();
        }
        return (map) -> expr.stream().allMatch(it -> Boolean.parseBoolean(it.evaluate(new MapContext(map)).toString()));
    }

}
