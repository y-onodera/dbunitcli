package yo.dbunitcli.dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RowFilters {

    public static final RowFilters NONE = new Builder().build();

    private final Map<String, RowFilter> byName = new HashMap<>();

    private final Map<String, RowFilter> pattern = new HashMap<>();

    private final RowFilter commonExpressions;

    protected RowFilters(final Builder builder) {
        this.byName.putAll(builder.byName);
        this.pattern.putAll(builder.pattern);
        this.commonExpressions = builder.commonExpressions;
    }

    public Builder builder() {
        return new Builder().add(this);
    }

    public RowFilters apply(final Consumer<RowFilters.Builder> editor) {
        final RowFilters.Builder builder = this.builder().add(this);
        editor.accept(builder);
        return builder.build();
    }

    public RowFilter getRowFilter(final String tableName) {
        RowFilter result = this.commonExpressions;
        if (this.byName.containsKey(tableName)) {
            result = result.add(this.byName.get(tableName), Function::compose);
        }
        return this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .reduce(result, (sum, param) -> sum.add(param, Function::andThen));
    }

    public RowFilters editCommonRenameFunction(final Function<Function<String, String>, Function<String, String>> editFunction) {
        return this.builder().add(this)
                .editCommonRenameFunction(editFunction)
                .build();
    }

    @Override
    public String toString() {
        return "RowFilters{" +
                "commonExpressions=" + this.commonExpressions +
                ", byName=" + this.byName +
                ", pattern=" + this.pattern +
                '}';
    }

    public static class Builder {
        private RowFilter commonExpressions = RowFilter.NONE;

        private final Map<String, RowFilter> byName = new HashMap<>();

        private final Map<String, RowFilter> pattern = new HashMap<>();

        public Builder add(final RowFilters rowFilters) {
            this.byName.putAll(rowFilters.byName);
            this.pattern.putAll(rowFilters.pattern);
            this.commonExpressions = this.commonExpressions.add(rowFilters.commonExpressions, (p1, p2) -> p2);
            return this;
        }

        public RowFilters build() {
            return new RowFilters(this);
        }

        public void addCommon(final String aExpression) {
            this.commonExpressions = this.commonExpressions.addFilter(aExpression);
        }

        public void add(final AddSettingColumns.Strategy strategy, final String key, final String expression) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                this.addSetting(key, expression, this.byName);
            } else if (strategy == AddSettingColumns.Strategy.PATTERN) {
                this.addSetting(key, expression, this.pattern);
            }
        }

        protected void addSetting(final String key, final String expression, final Map<String, RowFilter> filters) {
            if (filters.containsKey(key)) {
                filters.put(key, filters.get(key).addFilter(expression));
            } else {
                filters.put(key, new RowFilter(expression));
            }
        }

        public void addRenameFunction(final AddSettingColumns.Strategy strategy, final String key, final Function<String, String> toTableNameMapFunction) {
            Map<String, RowFilter> filter = this.byName;
            if (strategy == AddSettingColumns.Strategy.PATTERN) {
                filter = this.pattern;
            }
            filter.put(key, filter.getOrDefault(key, RowFilter.NONE).withRenameFunction(toTableNameMapFunction));
        }

        public Builder editCommonRenameFunction(final Function<Function<String, String>, Function<String, String>> editFunction) {
            this.commonExpressions = this.commonExpressions.editRenameFunction(editFunction);
            return this;
        }
    }
}
