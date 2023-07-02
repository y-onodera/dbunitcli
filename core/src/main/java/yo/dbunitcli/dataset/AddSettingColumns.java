package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record AddSettingColumns(
        Map<String, List<String>> byName
        , Map<String, List<String>> pattern
        , Map<String, ColumnExpression> byNameExpression
        , Map<String, ColumnExpression> patternExpression
        , Map<String, Boolean> byNameFlg
        , Map<String, Boolean> patternFlg
        , List<String> common
        , ColumnExpression commonExpression
        , boolean commonFlg
) {

    public static final AddSettingColumns NONE = new Builder().build();

    public static final String ALL_MATCH_PATTERN = "*";


    public AddSettingColumns(final Builder builder) {
        this(new HashMap<>()
                , new HashMap<>()
                , new HashMap<>()
                , new HashMap<>()
                , new HashMap<>()
                , new HashMap<>()
                , new ArrayList<>()
                , builder.commonExpression.build()
                , builder.commonFlg);
        this.byName.putAll(builder.byName);
        builder.byNameExpression.forEach((key, value) -> this.byNameExpression.put(key, value.build()));
        this.byNameFlg.putAll(builder.byNameFlg);
        this.pattern.putAll(builder.pattern);
        builder.patternExpression.forEach((key, value) -> this.patternExpression.put(key, value.build()));
        this.patternFlg.putAll(builder.patternFlg);
        this.common.addAll(builder.common);
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
        result.addAll(this.pattern.entrySet()
                .stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new ArrayList<>()));
        if (this.pattern.containsKey(ALL_MATCH_PATTERN)) {
            result.addAll(this.pattern.get(ALL_MATCH_PATTERN));
        }
        return result;
    }

    public ColumnExpression getExpression(final String tableName) {
        final ColumnExpression result = this.commonExpression;
        if (this.byNameExpression.containsKey(tableName)) {
            return result.add(this.byNameExpression.get(tableName));
        }
        final ColumnExpression patternResult = this.patternExpression.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .reduce(result, ColumnExpression::add);
        if (this.patternExpression.containsKey(ALL_MATCH_PATTERN)) {
            return this.patternExpression.get(ALL_MATCH_PATTERN).add(patternResult);
        }
        return patternResult;
    }

    public Boolean getFlg(final String tableName) {
        final Boolean result = this.commonFlg;
        if (this.byNameFlg.containsKey(tableName)) {
            return this.byNameFlg.get(tableName) || result;
        }
        final Boolean patternResult = this.patternFlg.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .reduce(result, (ret, newVal) -> ret || newVal);
        if (this.patternFlg.containsKey(ALL_MATCH_PATTERN)) {
            return this.patternFlg.get(ALL_MATCH_PATTERN) || result;
        }
        return patternResult;
    }

    public int byNameSize() {
        return this.byName.size();
    }

    public static class Builder {
        private final Map<String, List<String>> byName = new HashMap<>();

        private final Map<String, ColumnExpression.Builder> byNameExpression = new HashMap<>();

        private final Map<String, Boolean> byNameFlg = new HashMap<>();

        private final Map<String, List<String>> pattern = new HashMap<>();

        private final Map<String, ColumnExpression.Builder> patternExpression = new HashMap<>();

        private final Map<String, Boolean> patternFlg = new HashMap<>();

        private final List<String> common = new ArrayList<>();

        private final ColumnExpression.Builder commonExpression = ColumnExpression.builder();

        public boolean commonFlg;

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
                    .addByNameFlgFrom(from)
                    .addPatternFlgFrom(from)
                    .setCommonFlg(from.commonFlg)
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

        private Builder addPatternFlgFrom(final AddSettingColumns from) {
            this.patternFlg.putAll(from.patternFlg);
            return this;
        }

        private Builder addByNameFlgFrom(final AddSettingColumns from) {
            this.byNameFlg.putAll(from.byNameFlg);
            return this;
        }

        public Builder add(final Strategy strategy, final String name, final Boolean distinct) {
            if (strategy == Strategy.BY_NAME) {
                return this.addByNameFlg(name, distinct);
            } else if (strategy == Strategy.PATTERN) {
                return this.addPatternFlg(name, distinct);
            }
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

        public Builder addByNameFlg(final String name, final Boolean flg) {
            this.byNameFlg.put(name, flg);
            return this;
        }

        public Builder addPatternFlg(final String name, final Boolean flg) {
            this.patternFlg.put(name, flg);
            return this;
        }

        public Builder addCommon(final List<String> columns) {
            this.common.addAll(columns);
            return this;
        }

        public Builder setCommonFlg(final boolean flg) {
            this.commonFlg = flg;
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
