package yo.dbunitcli.dataset;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddSettingColumns {

    public static final AddSettingColumns NONE = AddSettingColumns.builder().build();

    public static final String ALL_MATCH_PATTERN = "*";

    private Map<String, List<String>> byName = Maps.newHashMap();

    private Map<String, List<String>> pattern = Maps.newHashMap();

    private Map<String, ColumnExpression> byNameExpression = Maps.newHashMap();

    private Map<String, ColumnExpression> patternExpression = Maps.newHashMap();

    private List<String> common = Lists.newArrayList();

    private ColumnExpression commonExpression;

    public AddSettingColumns(Builder builder) {
        this.byName.putAll(builder.byName);
        builder.byNameExpression.forEach((key, value) -> this.byNameExpression.put(key, value.build()));
        this.pattern.putAll(builder.pattern);
        builder.patternExpression.forEach((key, value) -> this.patternExpression.put(key, value.build()));
        this.common.addAll(builder.common);
        this.commonExpression = builder.commonExpression.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasAdditionalSetting(String tableName) {
        return this.byName.containsKey(tableName)
                || this.pattern.entrySet().stream().anyMatch(it -> it.getKey().equals(ALL_MATCH_PATTERN) || tableName.contains(it.getKey()));
    }

    public List<String> getColumns(String tableName) {
        List<String> result = Lists.newArrayList(this.common);
        if (this.byName.containsKey(tableName)) {
            result.addAll(this.byName.get(tableName));
            return result;
        }
        List<String> patternResult = this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        if (this.pattern.containsKey(ALL_MATCH_PATTERN) && patternResult.size() == 0) {
            result.addAll(this.pattern.get(ALL_MATCH_PATTERN));
        } else {
            result.addAll(patternResult);
        }
        return result;
    }

    public ColumnExpression getExpression(String tableName) {
        ColumnExpression result = this.commonExpression;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddSettingColumns that = (AddSettingColumns) o;
        return Objects.equal(byName, that.byName) &&
                Objects.equal(pattern, that.pattern) &&
                Objects.equal(byNameExpression, that.byNameExpression) &&
                Objects.equal(patternExpression, that.patternExpression) &&
                Objects.equal(common, that.common) &&
                Objects.equal(commonExpression, that.commonExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(byName, pattern, byNameExpression, patternExpression, common, commonExpression);
    }


    public static class Builder {
        private Map<String, List<String>> byName = Maps.newHashMap();

        private Map<String, ColumnExpression.Builder> byNameExpression = Maps.newHashMap();

        private Map<String, List<String>> pattern = Maps.newHashMap();

        private Map<String, ColumnExpression.Builder> patternExpression = Maps.newHashMap();

        private List<String> common = Lists.newArrayList();

        private ColumnExpression.Builder commonExpression = ColumnExpression.builder();

        public AddSettingColumns build() {
            return new AddSettingColumns(this);
        }

        public Builder add(Strategy strategy, String name, List<String> keys) {
            if (strategy == Strategy.BY_NAME) {
                return this.addByName(name, keys);
            } else if (strategy == Strategy.PATTERN) {
                return this.addPattern(name, keys);
            }
            return this;
        }

        public ColumnExpression.Builder getExpressionBuilder(Strategy strategy, String name) {
            if (strategy == Strategy.BY_NAME) {
                return this.getExpressionBuilder(name, this.byNameExpression);
            }
            return this.getExpressionBuilder(name, this.patternExpression);
        }

        protected ColumnExpression.Builder getExpressionBuilder(String name, Map<String, ColumnExpression.Builder> patternExpression) {
            if (patternExpression.containsKey(name)) {
                return patternExpression.get(name);
            } else {
                return patternExpression.put(name, ColumnExpression.builder());
            }
        }

        public Builder addByName(String name, List<String> keys) {
            this.byName.put(name, keys);
            return this;
        }

        public Builder addPattern(String name, List<String> keys) {
            this.pattern.put(name, keys);
            return this;
        }

        public Builder addCommon(List<String> columns) {
            this.common.addAll(columns);
            return this;
        }

        public ColumnExpression.Builder getCommonExpressionBuilder() {
            return this.commonExpression;
        }

    }

    public enum Strategy {
        BY_NAME, PATTERN
    }
}
