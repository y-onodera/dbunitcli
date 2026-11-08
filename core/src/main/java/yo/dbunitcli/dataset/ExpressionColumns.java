package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.resource.jexl.JexlEngines;
import yo.dbunitcli.resource.jexl.NumberParser;

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
                // 列の索引は大文字小文字を区別しないため、綴りだけ違う列が並ぶとどちらを指すか決まらない
                columnNames.stream()
                        .filter(it -> it.equalsIgnoreCase(column.getColumnName()) && !it.equals(column.getColumnName()))
                        .findFirst()
                        .ifPresent(it -> {
                            throw new IllegalArgumentException("expression column '" + column.getColumnName()
                                    + "' differs only in case from column '" + it + "'. use the same spelling.");
                        });
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
        final JexlContext jc = new MapContext(param);
        return this.values()
                .stream()
                .filter(exp -> exp.contains(columnName))
                .map(exp -> {
                    final Object val = JexlEngines.DATASET.createExpression(exp.values().get(columnName)).evaluate(jc);
                    return switch (exp.type()) {
                        case STRING -> val;
                        case BOOLEAN -> Boolean.parseBoolean(String.valueOf(val));
                        case SQL_FUNCTION -> Objects.toString(val, null);
                        case NUMBER -> val == null ? null : val.toString().isEmpty() ? "" : NumberParser.parse(val);
                    };
                })
                .reduce(null, (a, b) -> b != null ? b : a);
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
