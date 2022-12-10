package yo.dbunitcli.dataset;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RowFilters {

    public static final RowFilters NONE = new Builder().build();

    private final Map<String, List<RowFilter>> byNames = new HashMap<>();

    private final Map<String, List<RowFilter>> pattern = new HashMap<>();

    private final RowFilter commonExpressions;

    protected RowFilters(final Builder builder) {
        this.byNames.putAll(builder.getByNames());
        this.pattern.putAll(builder.getPattern());
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

    public List<RowFilter> getRowFilter(final String tableName) {
        final List<RowFilter> result = new ArrayList<>();
        if (this.byNames.containsKey(tableName)) {
            return this.byNames.get(tableName).stream()
                    .map(it -> this.commonExpressions.add(it, Function::compose))
                    .collect(Collectors.toList());
        }
        final Optional<List<RowFilter>> matches = this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
        if (matches.isPresent()) {
            return matches.get().stream()
                    .map(it -> this.commonExpressions.add(it, Function::compose))
                    .collect(Collectors.toList());
        }
        result.add(this.commonExpressions);
        return result;
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
                ", byNames=" + this.byNames +
                ", pattern=" + this.pattern +
                '}';
    }

    public static class Builder {
        private RowFilter commonExpressions = RowFilter.NONE;

        private final Map<String, List<RowFilter>> byNames = new HashMap<>();

        private final Map<String, List<RowFilter>> pattern = new HashMap<>();

        public Builder add(final RowFilters rowFilters) {
            this.byNames.putAll(rowFilters.byNames);
            this.pattern.putAll(rowFilters.pattern);
            this.commonExpressions = this.commonExpressions.add(rowFilters.commonExpressions, (p1, p2) -> p2);
            return this;
        }

        public RowFilters build() {
            return new RowFilters(this);
        }

        public Map<String, List<RowFilter>> getByNames() {
            return this.byNames;
        }

        public Map<String, List<RowFilter>> getPattern() {
            return this.pattern;
        }

        public void addCommon(final String aExpression) {
            this.commonExpressions = this.commonExpressions.addFilter(aExpression);
        }

        public void addSplit(final String targetName, final AddSettingColumns.Strategy strategy, final RowFilter splitResult) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                this.byNames.computeIfAbsent(targetName, key -> new ArrayList<>()).add(splitResult);
            } else {
                this.pattern.computeIfAbsent(targetName, key -> new ArrayList<>()).add(splitResult);
            }
        }

        public void add(final AddSettingColumns.Strategy strategy, final String key, final String expression) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                this.addSetting(key, expression, this.byNames);
            } else if (strategy == AddSettingColumns.Strategy.PATTERN) {
                this.addSetting(key, expression, this.pattern);
            }
        }

        protected void addSetting(final String key, final String expression, final Map<String, List<RowFilter>> filters) {
            if (filters.containsKey(key)) {
                filters.put(key, filters.get(key)
                        .stream()
                        .map(it -> it.addFilter(expression))
                        .collect(Collectors.toList()));
            } else {
                final List<RowFilter> newVal = new ArrayList<>();
                newVal.add(new RowFilter(expression));
                filters.put(key, newVal);
            }
        }

        public void addRenameFunction(final AddSettingColumns.Strategy strategy, final String key, final Function<String, String> toTableNameMapFunction) {
            Map<String, List<RowFilter>> filter = this.byNames;
            if (strategy == AddSettingColumns.Strategy.PATTERN) {
                filter = this.pattern;
            }
            if (filter.containsKey(key)) {
                filter.put(key, filter.get(key)
                        .stream()
                        .map(it -> it.withRenameFunction(toTableNameMapFunction))
                        .collect(Collectors.toList()));
            } else {
                final List<RowFilter> newVal = new ArrayList<>();
                newVal.add(new RowFilter(new ArrayList<>(), toTableNameMapFunction));
                filter.put(key, newVal);
            }
        }

        public Builder editCommonRenameFunction(final Function<Function<String, String>, Function<String, String>> editFunction) {
            this.commonExpressions = this.commonExpressions.editRenameFunction(editFunction);
            return this;
        }
    }
}
