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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RowFilter {

    public static final RowFilter NONE = builder().build();

    private final Set<String> commonExpressions = Sets.newHashSet();

    private final Map<String, Set<String>> byName = Maps.newHashMap();

    private final Map<String, Set<String>> pattern = Maps.newHashMap();

    protected RowFilter(final Builder builder) {
        this.byName.putAll(builder.byName);
        this.pattern.putAll(builder.pattern);
        this.commonExpressions.addAll(builder.commonExpressions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public RowFilter apply(final Consumer<RowFilter.Builder> editor) {
        final RowFilter.Builder builder = builder().add(this);
        editor.accept(builder);
        return builder.build();
    }

    public Predicate<Map<String, Object>> getRowFilter(final String tableName) {
        final Set<String> expressions = this.expressions(tableName);
        if (expressions.size() == 0) {
            return null;
        }
        final JexlEngine jexl = new JexlBuilder().create();
        final List<JexlExpression> expr = expressions
                .stream()
                .map(jexl::createExpression)
                .collect(Collectors.toList());
        return (map) -> expr.stream().allMatch(it -> Boolean.parseBoolean(it.evaluate(new MapContext(map)).toString()));
    }

    public Set<String> expressions(final String tableName) {
        final Set<String> result = Sets.newLinkedHashSet(this.commonExpressions);
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

    @Override
    public String toString() {
        return "RowFilter{" +
                "commonExpressions=" + this.commonExpressions +
                ", byName=" + this.byName +
                ", pattern=" + this.pattern +
                '}';
    }

    public static class Builder {
        private final Set<String> commonExpressions = Sets.newHashSet();

        private final Map<String, Set<String>> byName = Maps.newHashMap();

        private final Map<String, Set<String>> pattern = Maps.newHashMap();

        public Builder add(final RowFilter rowFilter) {
            this.byName.putAll(rowFilter.byName);
            this.pattern.putAll(rowFilter.pattern);
            this.commonExpressions.addAll(rowFilter.commonExpressions);
            return this;
        }

        public RowFilter build() {
            return new RowFilter(this);
        }

        public void addCommon(final String aExpression) {
            this.commonExpressions.add(aExpression);
        }

        public void add(final AddSettingColumns.Strategy strategy, final String key, final String expression) {
            if (strategy == AddSettingColumns.Strategy.BY_NAME) {
                this.addSetting(key, expression, this.byName);
            } else if (strategy == AddSettingColumns.Strategy.PATTERN) {
                this.addSetting(key, expression, this.pattern);
            }
        }

        protected void addSetting(final String key, final String expression, final Map<String, Set<String>> filters) {
            if (!filters.containsKey(key)) {
                filters.put(key, new HashSet<>());
            }
            filters.get(key).add(expression);
        }
    }
}
