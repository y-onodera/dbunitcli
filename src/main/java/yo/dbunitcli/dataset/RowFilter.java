package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RowFilter {

    public static final RowFilter NONE = builder().build();

    private Set<String> commonExpressions = Sets.newHashSet();

    private Map<String, Set<String>> byName = Maps.newHashMap();

    private Map<String, Set<String>> pattern = Maps.newHashMap();

    protected RowFilter(Builder builder) {
        this.byName.putAll(builder.byName);
        this.pattern.putAll(builder.pattern);
        this.commonExpressions.addAll(builder.commonExpressions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Predicate<Map<String, Object>> getRowFilter(String tableName) {
        Set<String> expressions = this.expressions(tableName);
        if (expressions.size() == 0) {
            return null;
        }
        JexlEngine jexl = new JexlBuilder().create();
        List<JexlExpression> expr = expressions
                .stream()
                .map(jexl::createExpression)
                .collect(Collectors.toList());
        return (map) -> expr.stream().allMatch(it -> Boolean.parseBoolean(it.evaluate(new MapContext(map)).toString()));
    }

    public Set<String> expressions(String tableName) {
        Set<String> result = Sets.newLinkedHashSet(this.commonExpressions);
        if (this.byName.containsKey(tableName)) {
            result.addAll(this.byName.get(tableName));
            return result;
        }
        result.addAll(this.pattern.entrySet().stream()
                .filter(it -> tableName.contains(it.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .collect(Collectors.toSet()));
        return result;
    }

    public static class Builder {
        private Set<String> commonExpressions = Sets.newHashSet();

        private Map<String, Set<String>> byName = Maps.newHashMap();

        private Map<String, Set<String>> pattern = Maps.newHashMap();

        public RowFilter build() {
            return new RowFilter(this);
        }

        public void addCommon(String aExpression) {
            commonExpressions.add(aExpression);
        }

        public void add(AddSettingColumns.Strategy strategy, String key, String expression) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                addSetting(key, expression, this.byName);
            } else if (strategy == AddSettingColumns.Strategy.PATTERN) {
                addSetting(key, expression, this.pattern);
            }
        }

        public void appendTo(Builder other) {
            this.byName.forEach((key, value1) -> {
                if (other.byName.containsKey(key)) {
                    Set<String> value = other.byName.get(key);
                    value.addAll(value1);
                    other.byName.put(key, value);
                } else {
                    other.byName.put(key, value1);
                }
            });
            this.pattern.forEach((key, value1) -> {
                if (other.pattern.containsKey(key)) {
                    Set<String> value = other.pattern.get(key);
                    value.addAll(value1);
                    other.pattern.put(key, value);
                } else {
                    other.pattern.put(key, value1);
                }
            });
            other.commonExpressions.addAll(this.commonExpressions);
        }

        protected void addSetting(String key, String expression, Map<String, Set<String>> filters) {
            if (!filters.containsKey(key)) {
                filters.put(key, new HashSet<>());
            }
            filters.get(key).add(expression);
        }
    }
}
