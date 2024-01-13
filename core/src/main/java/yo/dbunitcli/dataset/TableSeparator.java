package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record TableSeparator(TargetFilter targetFilter
        , TableSplitter splitter
        , List<String> comparisonKeys
        , ExpressionColumns expressionColumns
        , List<String> includeColumns
        , List<String> excludeColumns
        , List<String> orderColumns
        , Predicate<Map<String, Object>> filter
        , boolean distinct
) {

    public static final TargetFilter ACCEPT_ALL = new TargetFilter.Always(true);
    public static final TargetFilter REJECT_ALL = new TargetFilter.Always(false);
    public static final Predicate<Map<String, Object>> NO_FILTER = it -> Boolean.TRUE;

    public static final TableSeparator NONE = TableSeparator.builder().build();

    public static Builder builder() {
        return new Builder();
    }

    public TableSeparator(final Builder builder) {
        this(builder.getTargetFilter()
                , builder.getSplitter()
                , new ArrayList<>(builder.getComparisonKeys())
                , builder.getExpressionColumns().copy()
                , new ArrayList<>(builder.getIncludeColumns())
                , new ArrayList<>(builder.getExcludeColumns())
                , new ArrayList<>(builder.getOrderColumns())
                , builder.getFilter()
                , builder.isDistinct()
        );
    }

    public AddSettingTableMetaData addSetting(final ITableMetaData originMetaData) {
        final Column[] comparisonKeys = this.getComparisonKeys();
        try {
            Column[] primaryKey = originMetaData.getPrimaryKeys();
            if (comparisonKeys.length > 0) {
                primaryKey = comparisonKeys;
            }
            return AddSettingTableMetaData.builder(originMetaData, primaryKey, this).build();
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public TableSeparator map(final Consumer<Builder> mapper) {
        final Builder builder = new Builder(this);
        mapper.accept(builder);
        return builder.build();
    }

    public Column[] getComparisonKeys() {
        return this.comparisonKeys()
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    public IColumnFilter getColumnFilter() {
        if (this.excludeColumns.size() == 0 && this.includeColumns.size() == 0) {
            return null;
        }
        final DefaultColumnFilter result = new DefaultColumnFilter();
        if (this.includeColumns.size() > 0) {
            result.includeColumns(this.includeColumns.stream()
                    .map(it -> new Column(it, DataType.UNKNOWN))
                    .toList().toArray(new Column[0]));
        }
        if (this.excludeColumns.size() > 0) {
            result.excludeColumns(this.excludeColumns.stream()
                    .map(it -> new Column(it, DataType.UNKNOWN))
                    .toList().toArray(new Column[0]));
        }
        return result;
    }

    public Column[] getOrderColumns() {
        return this.orderColumns.stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toList().toArray(new Column[0]);
    }

    public boolean hasRowFilter() {
        return this.filter() != TableSeparator.NO_FILTER;
    }

    public boolean test(final Map<String, Object> rowToMap) {
        return this.filter.test(rowToMap);
    }

    public String rename(final String tableName) {
        if (this.splitter.limit() > 0) {
            return tableName;
        }
        return this.splitter.renameFunction().apply(tableName, 0);
    }

    public TableSeparator add(final TableSeparator other) {
        return this.map(builder -> builder.with(other));
    }

    public boolean hasSettings() {
        return !this.splitter.equals(TableSplitter.NONE)
                || this.comparisonKeys.size() > 0
                || this.expressionColumns.size() > 0
                || this.includeColumns.size() > 0
                || this.excludeColumns.size() > 0
                || this.orderColumns.size() > 0
                || this.filter != TableSeparator.NO_FILTER
                || this.distinct
                ;
    }

    public interface TargetFilter {

        static TargetFilter any(final String... names) {
            return new Any(List.of(names));
        }

        static TargetFilter contain(final String pattern) {
            return new Contain(pattern);
        }

        default TargetFilter exclude(final List<String> names) {
            return new Exclude(this, names);
        }

        boolean test(String tableName);

        record Exclude(TargetFilter base, List<String> names) implements TargetFilter {
            @Override
            public boolean test(final String tableName) {
                return this.base.test(tableName) && !this.names().contains(tableName);
            }
        }

        record Any(List<String> names) implements TargetFilter {
            @Override
            public boolean test(final String tableName) {
                return this.names().contains(tableName);
            }
        }

        record Contain(String patternString) implements TargetFilter {
            @Override
            public boolean test(final String tableName) {
                return tableName.contains(this.patternString()) || this.patternString().equals("*");
            }
        }

        record Always(boolean result) implements TargetFilter {
            @Override
            public boolean test(final String tableName) {
                return this.result();
            }
        }

    }

    public static class Builder {
        private TargetFilter targetFilter = TableSeparator.ACCEPT_ALL;
        private TableSplitter splitter = TableSplitter.NONE;
        private List<String> comparisonKeys = new ArrayList<>();
        private ExpressionColumns expressionColumns = ExpressionColumns.NONE;
        private List<String> includeColumns = new ArrayList<>();
        private List<String> excludeColumns = new ArrayList<>();
        private List<String> orderColumns = new ArrayList<>();
        private Predicate<Map<String, Object>> filter = TableSeparator.NO_FILTER;

        private boolean distinct = false;

        private Builder() {
        }

        public Builder(final TableSeparator tableSeparator) {
            this.targetFilter = tableSeparator.targetFilter();
            this.splitter = tableSeparator.splitter();
            this.comparisonKeys.addAll(tableSeparator.comparisonKeys());
            this.expressionColumns = tableSeparator.expressionColumns().copy();
            this.includeColumns.addAll(tableSeparator.includeColumns());
            this.excludeColumns.addAll(tableSeparator.excludeColumns());
            this.orderColumns.addAll(tableSeparator.orderColumns());
            this.filter = tableSeparator.filter();
            this.distinct = tableSeparator.distinct();
        }

        public Builder with(final TableSeparator other) {
            return this.with(other.splitter())
                    .with(other.filter())
                    .setComparisonKeys(Stream.concat(this.comparisonKeys.stream(), other.comparisonKeys().stream())
                            .distinct()
                            .toList())
                    .setExpressionColumns(this.expressionColumns.add(other.expressionColumns()))
                    .setIncludeColumns(Stream.concat(this.includeColumns.stream(), other.includeColumns().stream())
                            .distinct()
                            .toList())
                    .setExcludeColumns(Stream.concat(this.excludeColumns.stream(), other.excludeColumns().stream())
                            .distinct()
                            .toList())
                    .setOrderColumns(Stream.concat(this.orderColumns.stream(), other.orderColumns().stream())
                            .distinct()
                            .toList())
                    .setDistinct(this.isDistinct() || other.distinct())
                    ;
        }

        public Builder with(final TableSplitter other) {
            if (this.splitter == TableSplitter.NONE) {
                return this.setSplitter(other);
            } else if (other == TableSplitter.NONE || this.splitter.equals(other)) {
                return this;
            } else if (!other.isSplit() && !this.splitter.renameFunction().equals(other.renameFunction())) {
                return this.setSplitter(this.splitter.builder()
                        .setRenameFunction(this.splitter.renameFunction().compose(other.renameFunction()))
                        .build());
            } else if (!this.splitter.isSplit() && other.isSplit() && !this.splitter.renameFunction().equals(other.renameFunction())) {
                return this.setSplitter(other.builder()
                        .setRenameFunction(this.splitter.renameFunction().andThen(other.renameFunction()))
                        .build());
            }
            throw new UnsupportedOperationException("splitter define is conflict:" + this);
        }

        public Builder with(final Predicate<Map<String, Object>> otherFilter) {
            if (otherFilter == TableSeparator.NONE.filter()) {
                return this;
            } else if (this.filter == TableSeparator.NONE.filter()) {
                return this.setFilter(otherFilter);
            }
            return this.setFilter(this.filter.and(otherFilter));
        }

        public TargetFilter getTargetFilter() {
            return this.targetFilter;
        }

        public TableSplitter getSplitter() {
            return this.splitter;
        }

        public List<String> getComparisonKeys() {
            return this.comparisonKeys;
        }

        public List<String> getIncludeColumns() {
            return this.includeColumns;
        }

        public List<String> getExcludeColumns() {
            return this.excludeColumns;
        }

        public List<String> getOrderColumns() {
            return this.orderColumns;
        }

        public Predicate<Map<String, Object>> getFilter() {
            return this.filter;
        }

        public boolean isDistinct() {
            return this.distinct;
        }

        public ExpressionColumns getExpressionColumns() {
            return this.expressionColumns;
        }

        public Builder setTargetFilter(final TargetFilter targetFilter) {
            this.targetFilter = targetFilter;
            return this;
        }

        public Builder setSplitter(final TableSplitter splitter) {
            this.splitter = splitter;
            return this;
        }

        public Builder setComparisonKeys(final List<String> comparisonKeys) {
            this.comparisonKeys = comparisonKeys;
            return this;
        }

        public Builder setIncludeColumns(final List<String> includeColumns) {
            this.includeColumns = includeColumns;
            return this;
        }

        public Builder setExcludeColumns(final List<String> excludeColumns) {
            this.excludeColumns = excludeColumns;
            return this;
        }

        public Builder setOrderColumns(final List<String> orderColumns) {
            this.orderColumns = orderColumns;
            return this;
        }

        public Builder setFilter(final Predicate<Map<String, Object>> filter) {
            this.filter = filter;
            return this;
        }

        public Builder setFilter(final List<String> filterExpressions) {
            this.filter = this.createFilter(filterExpressions);
            return this;
        }

        public Builder setDistinct(final boolean distinct) {
            this.distinct = distinct;
            return this;
        }

        public Builder setExpressionColumns(final ExpressionColumns expressionColumns) {
            this.expressionColumns = expressionColumns;
            return this;
        }

        public TableSeparator build() {
            return new TableSeparator(this);
        }

        private Predicate<Map<String, Object>> createFilter(final List<String> expressions) {
            final JexlEngine jexl = new JexlBuilder().create();
            final List<JexlExpression> expr = expressions
                    .stream()
                    .filter(it -> !it.trim().isEmpty())
                    .map(jexl::createExpression)
                    .toList();
            if (expr.size() == 0) {
                return TableSeparator.NONE.filter();
            }
            return map -> expr.stream().allMatch(it -> Boolean.parseBoolean(it.evaluate(new MapContext(map)).toString()));
        }
    }
}
