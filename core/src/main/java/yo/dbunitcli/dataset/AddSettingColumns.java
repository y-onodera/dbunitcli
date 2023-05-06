package yo.dbunitcli.dataset;

import java.util.*;
import java.util.function.Consumer;

public class AddSettingColumns {

    public static final AddSettingColumns NONE = new Builder().build();

    public static final String ALL_MATCH_PATTERN = "*";

    private final Map<String, List<String>> byName = new HashMap<>();

    private final Map<String, List<String>> pattern = new HashMap<>();

    private final Map<String, ColumnExpression> byNameExpression = new HashMap<>();

    private final Map<String, ColumnExpression> patternExpression = new HashMap<>();

    private final List<String> common = new ArrayList<>();

    private final ColumnExpression commonExpression;

    public AddSettingColumns(final Builder builder) {
        this.byName.putAll(builder.byName);
        builder.byNameExpression.forEach((key, value) -> this.byNameExpression.put(key, value.build()));
        this.pattern.putAll(builder.pattern);
        builder.patternExpression.forEach((key, value) -> this.patternExpression.put(key, value.build()));
        this.common.addAll(builder.common);
        this.commonExpression = builder.commonExpression.build();
    }

    public Builder builder() {
        return new Builder().add(this);
    }

    public AddSettingColumns apply(final Consumer<Builder> editor) {
        final Builder builder = this.builder().add(this);
        editor.accept(builder);
        return builder.build();
    }

    public boolean hasAdditionalSetting(final String tableName) {
        return this.byName.containsKey(tableName)
                || this.pattern.entrySet().stream().anyMatch(it -> it.getKey().equals(ALL_MATCH_PATTERN) || tableName.contains(it.getKey()));
    }

    public List<String> getColumns(final String tableName) {
        final List<String> result = new ArrayList<>(this.common);
        if (this.byName.containsKey(tableName)) {
            result.addAll(this.byName.get(tableName));
            return result;
        }
        final List<String> patternResult = this.pattern.entrySet()
                .stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new ArrayList<>());
        if (this.pattern.containsKey(ALL_MATCH_PATTERN) && patternResult.size() == 0) {
            result.addAll(this.pattern.get(ALL_MATCH_PATTERN));
        } else {
            result.addAll(patternResult);
        }
        return result;
    }

    public ColumnExpression getExpression(final String tableName) {
        final ColumnExpression result = this.commonExpression;
        if (this.byNameExpression.containsKey(tableName)) {
            return result.add(this.byNameExpression.get(tableName));
        }
        return this.patternExpression.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .reduce(result, ColumnExpression::add);
    }

    public int byNameSize() {
        return this.byName.size();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddSettingColumns)) {
            return false;
        }
        final AddSettingColumns that = (AddSettingColumns) o;
        return Objects.equals(this.byName, that.byName)
                && Objects.equals(this.pattern, that.pattern)
                && Objects.equals(this.byNameExpression, that.byNameExpression)
                && Objects.equals(this.patternExpression, that.patternExpression)
                && Objects.equals(this.common, that.common)
                && Objects.equals(this.commonExpression, that.commonExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.byName, this.pattern, this.byNameExpression, this.patternExpression, this.common, this.commonExpression);
    }

    @Override
    public String toString() {
        return "AddSettingColumns{" +
                "byName=" + this.byName +
                ", pattern=" + this.pattern +
                ", byNameExpression=" + this.byNameExpression +
                ", patternExpression=" + this.patternExpression +
                ", common=" + this.common +
                ", commonExpression=" + this.commonExpression +
                '}';
    }

    public static class Builder {
        private final Map<String, List<String>> byName = new HashMap<>();

        private final Map<String, ColumnExpression.Builder> byNameExpression = new HashMap<>();

        private final Map<String, List<String>> pattern = new HashMap<>();

        private final Map<String, ColumnExpression.Builder> patternExpression = new HashMap<>();

        private final List<String> common = new ArrayList<>();

        private final ColumnExpression.Builder commonExpression = ColumnExpression.builder();

        public AddSettingColumns build() {
            return new AddSettingColumns(this);
        }

        public Builder add(final AddSettingColumns from) {
            return this.addByNameExpressionFrom(from)
                    .addPatternExpressionFrom(from)
                    .addCommonExpressionFrom(from)
                    .addByNameFrom(from)
                    .addPatternFrom(from)
                    .addCommon(from.common)
                    ;
        }

        private Builder addPatternFrom(final AddSettingColumns from) {
            from.pattern.forEach(this::addPattern);
            return this;
        }

        public Builder addByNameFrom(final AddSettingColumns from) {
            from.byName.forEach(this::addByName);
            return this;
        }

        public Builder addCommonExpressionFrom(final AddSettingColumns from) {
            this.getCommonExpressionBuilder().add(from.commonExpression);
            return this;
        }

        public Builder addPatternExpressionFrom(final AddSettingColumns from) {
            from.patternExpression.forEach((key, value) -> this.getExpressionBuilder(Strategy.PATTERN, key).add(value));
            return this;
        }

        public Builder addByNameExpressionFrom(final AddSettingColumns from) {
            from.byNameExpression.forEach((key, value) -> this.getExpressionBuilder(Strategy.BY_NAME, key).add(value));
            return this;
        }

        public Builder add(final Strategy strategy, final String name, final List<String> keys) {
            if (strategy == Strategy.BY_NAME) {
                return this.addByName(name, keys);
            } else if (strategy == Strategy.PATTERN) {
                return this.addPattern(name, keys);
            }
            return this;
        }

        public ColumnExpression.Builder getExpressionBuilder(final Strategy strategy, final String name) {
            if (strategy == Strategy.BY_NAME) {
                return this.getExpressionBuilder(name, this.byNameExpression);
            }
            return this.getExpressionBuilder(name, this.patternExpression);
        }

        protected ColumnExpression.Builder getExpressionBuilder(final String name, final Map<String, ColumnExpression.Builder> patternExpression) {
            if (patternExpression.containsKey(name)) {
                return patternExpression.get(name);
            }
            patternExpression.put(name, ColumnExpression.builder());
            return patternExpression.get(name);
        }

        public Builder addByName(final String name, final List<String> keys) {
            this.byName.put(name, keys);
            return this;
        }

        public Builder addPattern(final String name, final List<String> keys) {
            this.pattern.put(name, keys);
            return this;
        }

        public Builder addCommon(final List<String> columns) {
            this.common.addAll(columns);
            return this;
        }

        public ColumnExpression.Builder getCommonExpressionBuilder() {
            return this.commonExpression;
        }

        public Map<String, List<String>> getByName() {
            return this.byName;
        }

        public Map<String, ColumnExpression.Builder> getByNameExpression() {
            return this.byNameExpression;
        }

        public Map<String, List<String>> getPattern() {
            return this.pattern;
        }

        public Map<String, ColumnExpression.Builder> getPatternExpression() {
            return this.patternExpression;
        }

        public List<String> getCommon() {
            return this.common;
        }

    }

    public enum Strategy {
        BY_NAME, PATTERN
    }
}
