package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public record ExpressionColumns(List<Expressions> values) {

    public static final ExpressionColumns NONE = new Builder().build();

    public static Builder builder() {
        return new Builder();
    }

    public ExpressionColumns(final Builder builder) {
        this(new ArrayList<>(builder.expressions));
    }

    public ExpressionColumns copy() {
        return builder().add(this).build();
    }

    public ExpressionColumns add(final ExpressionColumns other) {
        return builder().add(this).add(other).build();
    }

    public Collection<? extends Column> toColumns() {
        final Set<Column> result = new LinkedHashSet<>();
        this.values.forEach(it -> {
            switch (it.type) {
                case STRING -> it.values().keySet().forEach(key -> result.add(new Column(key, DataType.NVARCHAR)));
                case BOOLEAN -> it.values().keySet().forEach(key -> result.add(new Column(key, DataType.BOOLEAN)));
                case NUMBER, SQL_FUNCTION ->
                        it.values().keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
            }
        });
        return result;
    }

    public Column[] merge(final Column[] columns) {
        if (this.size() > 0) {
            final ArrayList<Column> columnList = Arrays.stream(columns).collect(Collectors.toCollection(ArrayList::new));
            final List<String> columnNames = columnList.stream().map(Column::getColumnName).toList();
            this.toColumns().forEach(column -> {
                if (!columnNames.contains(column.getColumnName())) {
                    columnList.add(column);
                } else {
                    columnList.replaceAll(it -> it.getColumnName().equals(column.getColumnName()) ? column : it);
                }
            });
            return columnList.toArray(new Column[0]);
        }
        return columns;
    }

    public int size() {
        return this.values().stream().map(it -> it.values().size()).reduce(0, Integer::sum);
    }

    public boolean contains(final String columnName) {
        return this.values().stream().map(it -> it.contains(columnName))
                .reduce(false, (result, newVal) -> result || newVal);
    }

    public Object evaluate(final String columnName, final Map<String, Object> param) {
        final JexlEngine jexl = new JexlBuilder().create();
        final JexlContext jc = new MapContext(param);
        return this.values()
                .stream()
                .filter(exp -> exp.contains(columnName))
                .map(exp ->
                        switch (exp.type()) {
                            case STRING -> jexl.createExpression(exp.values().get(columnName)).evaluate(jc);
                            case BOOLEAN ->
                                    Boolean.parseBoolean(jexl.createExpression(exp.values().get(columnName)).evaluate(jc).toString());
                            case SQL_FUNCTION ->
                                    jexl.createExpression(exp.values().get(columnName)).evaluate(jc).toString();
                            case NUMBER -> {
                                final String val = jexl.createExpression(exp.values().get(columnName)).evaluate(jc).toString();
                                if (val.isEmpty()) {
                                    yield val;
                                }
                                yield new BigDecimal(val);
                            }
                        })
                .findFirst()
                .orElse("");
    }

    public enum ParameterType {
        STRING, BOOLEAN, NUMBER, SQL_FUNCTION {
            @Override
            public String keyName() {
                return "sqlFunction";
            }
        };

        public String keyName() {
            return this.name().toLowerCase();
        }
    }

    public record Expressions(ParameterType type, Map<String, String> values) {
        public boolean contains(final String columnName) {
            return this.values().containsKey(columnName);
        }
    }

    public static class Builder {
        private final List<Expressions> expressions = new ArrayList<>();

        public Builder add(final ExpressionColumns expressionColumns) {
            expressionColumns.values()
                    .stream()
                    .map(it -> new Expressions(it.type(), new LinkedHashMap<>(it.values())))
                    .forEach(this::addExpressions);
            return this;
        }

        public ExpressionColumns build() {
            return new ExpressionColumns(this);
        }

        public Builder addExpressions(final Expressions newVal) {
            this.expressions.stream().filter(it -> it.type() == newVal.type)
                    .findFirst()
                    .ifPresentOrElse(it -> it.values().putAll(newVal.values()), () -> this.expressions.add(newVal));
            return this;
        }
    }

}
