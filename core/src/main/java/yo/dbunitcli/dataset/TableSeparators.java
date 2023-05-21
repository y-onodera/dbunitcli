package yo.dbunitcli.dataset;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record TableSeparators(Map<String, List<TableSeparator>> byNames,
                              Map<String, List<TableSeparator>> pattern,
                              TableSeparator commonExpressions) {

    public static final TableSeparators NONE = new Builder().build();

    TableSeparators(final Builder builder) {
        this(new HashMap<>(builder.getByNames()), new HashMap<>(builder.getPattern()), builder.commonExpressions);
    }

    public Builder builder() {
        return new Builder().add(this);
    }

    public TableSeparators apply(final Consumer<TableSeparators.Builder> editor) {
        final TableSeparators.Builder builder = this.builder().add(this);
        editor.accept(builder);
        return builder.build();
    }

    public Collection<TableSeparator> getSeparators(final String tableName) {
        final Set<TableSeparator> result = new HashSet<>();
        if (this.byNames.containsKey(tableName)) {
            result.addAll(this.byNames.get(tableName).stream()
                    .map(this.commonExpressions::add)
                    .toList());
        }
        result.addAll(this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .map(this.commonExpressions::add)
                .toList());
        if (result.size() > 0) {
            return result;
        }
        result.add(this.commonExpressions);
        return result;
    }

    public static class Builder {
        private TableSeparator commonExpressions = TableSeparator.NONE;

        private final Map<String, List<TableSeparator>> byNames = new HashMap<>();

        private final Map<String, List<TableSeparator>> pattern = new HashMap<>();

        public Builder add(final TableSeparators tableSeparators) {
            this.byNames.putAll(tableSeparators.byNames);
            this.pattern.putAll(tableSeparators.pattern);
            this.commonExpressions = this.commonExpressions.add(tableSeparators.commonExpressions);
            return this;
        }

        public TableSeparators build() {
            return new TableSeparators(this);
        }

        public Map<String, List<TableSeparator>> getByNames() {
            return this.byNames;
        }

        public Map<String, List<TableSeparator>> getPattern() {
            return this.pattern;
        }

        public void addCommon(final List<String> aExpressions) {
            this.commonExpressions = this.commonExpressions.addFilter(aExpressions);
        }

        public void add(final AddSettingColumns.Strategy strategy, final String targetName, final TableSeparator filter) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                this.add(this.byNames, targetName, filter);
            } else {
                this.add(this.pattern, targetName, filter);
            }
        }

        public void add(final AddSettingColumns.Strategy strategy, final String targetName, final List<String> expressions, final TableSplitter splitter) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                this.addSetting(this.byNames, targetName, expressions, splitter);
            } else if (strategy == AddSettingColumns.Strategy.PATTERN) {
                this.addSetting(this.pattern, targetName, expressions, splitter);
            }
        }

        public Builder setCommonRenameFunction(final TableRenameStrategy newFunction) {
            this.commonExpressions = this.commonExpressions.with(this.commonExpressions
                    .splitter()
                    .builder()
                    .setRenameFunction(newFunction)
                    .build());
            return this;
        }

        protected void add(final Map<String, List<TableSeparator>> byNames, final String targetName, final TableSeparator separator) {
            final List<TableSeparator> current = byNames.computeIfAbsent(targetName, key -> new ArrayList<>());
            if (!current.contains(separator)) {
                current.add(separator);
            }
        }

        protected void addSetting(final Map<String, List<TableSeparator>> filters, final String targetName, final List<String> expressions, final TableSplitter splitter) {
            if (filters.containsKey(targetName)) {
                filters.put(targetName, filters.get(targetName)
                        .stream()
                        .map(it -> it.addFilter(expressions).with(splitter))
                        .collect(Collectors.toList()));
            } else {
                final List<TableSeparator> newVal = new ArrayList<>();
                newVal.add(new TableSeparator(splitter, expressions));
                filters.put(targetName, newVal);
            }
        }

    }
}
