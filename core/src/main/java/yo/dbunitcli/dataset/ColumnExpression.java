package yo.dbunitcli.dataset;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.IColumnFilter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public record ColumnExpression(
        Map<String, String> stringExpression
        , Map<String, String> booleanExpression
        , Map<String, String> numberExpression
        , Map<String, String> sqlFunction
) {

    public ColumnExpression(final Builder builder) {
        this(new LinkedHashMap<>(builder.stringExpression)
                , new LinkedHashMap<>(builder.booleanExpression)
                , new LinkedHashMap<>(builder.numberExpression)
                , new LinkedHashMap<>(builder.sqlFunction)
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public ColumnExpression add(final ColumnExpression other) {
        return builder().add(this).add(other).build();
    }


    public AddSettingTableMetaData apply(final ITableMetaData delegateMetaData) {
        try {
            return this.apply(delegateMetaData, null, delegateMetaData.getPrimaryKeys(), TableSeparator.NONE, Boolean.FALSE);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public AddSettingTableMetaData apply(final ITableMetaData originMetaData, final IColumnFilter iColumnFilter, final Column[] comparisonKeys, final TableSeparator tableSeparator, final Boolean distinct) {
        try {
            Column[] primaryKey = originMetaData.getPrimaryKeys();
            if (comparisonKeys.length > 0) {
                primaryKey = comparisonKeys;
            }
            return new AddSettingTableMetaData(originMetaData, primaryKey, iColumnFilter, tableSeparator, distinct, this);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public Collection<? extends Column> getColumns() {
        final Set<Column> result = new LinkedHashSet<>();
        this.stringExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NVARCHAR)));
        this.booleanExpression.keySet().forEach(key -> result.add(new Column(key, DataType.BOOLEAN)));
        this.numberExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        this.sqlFunction.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        return result;
    }

    public Column[] merge(final Column[] columns) {
        if (this.size() > 0) {
            final ArrayList<Column> columnList = Arrays.stream(columns).collect(Collectors.toCollection(ArrayList::new));
            final List<String> columnNames = columnList.stream().map(Column::getColumnName).toList();
            this.getColumns().forEach(column -> {
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
        return this.stringExpression.size()
                + this.booleanExpression.size()
                + this.numberExpression.size()
                + this.sqlFunction.size()
                ;
    }

    public boolean contains(final String columnName) {
        return this.stringExpression.containsKey(columnName)
                || this.booleanExpression.containsKey(columnName)
                || this.numberExpression.containsKey(columnName)
                || this.sqlFunction.containsKey(columnName)
                ;
    }

    public Object evaluate(final String columnName, final Map<String, Object> param) {
        final JexlEngine jexl = new JexlBuilder().create();
        final JexlContext jc = new MapContext(param);
        if (this.stringExpression.containsKey(columnName)) {
            return jexl.createExpression(this.stringExpression.get(columnName)).evaluate(jc);
        } else if (this.booleanExpression.containsKey(columnName)) {
            return Boolean.parseBoolean(jexl.createExpression(this.booleanExpression.get(columnName)).evaluate(jc).toString());
        } else if (this.sqlFunction.containsKey(columnName)) {
            return jexl.createExpression(this.sqlFunction.get(columnName)).evaluate(jc).toString();
        }
        return new BigDecimal(jexl.createExpression(this.numberExpression.get(columnName)).evaluate(jc).toString());
    }

    public static class Builder {
        private final Map<String, String> stringExpression = new LinkedHashMap<>();

        private final Map<String, String> booleanExpression = new LinkedHashMap<>();

        private final Map<String, String> numberExpression = new LinkedHashMap<>();

        private final Map<String, String> sqlFunction = new LinkedHashMap<>();

        public Builder add(final ColumnExpression columnExpression) {
            return this.addStringExpression(columnExpression.stringExpression)
                    .addBooleanExpression(columnExpression.booleanExpression)
                    .addNumberExpression(columnExpression.numberExpression)
                    .addSqlFunction(columnExpression.sqlFunction);
        }

        public ColumnExpression build() {
            return new ColumnExpression(this);
        }

        public void addExpression(final ParameterType type, final String key, final String value) {
            switch (type) {
                case STRING:
                    this.addStringExpression(key, value);
                    return;
                case BOOLEAN:
                    this.addBooleanExpression(key, value);
                    return;
                case NUMBER:
                    this.addNumberExpression(key, value);
                case SQL_FUNCTION:
                    this.addSqlFunction(key, value);
            }
        }

        public Builder addStringExpression(final Map<String, String> stringExpression) {
            stringExpression.forEach(this::addStringExpression);
            return this;
        }

        public Builder addBooleanExpression(final Map<String, String> booleanExpression) {
            booleanExpression.forEach(this::addBooleanExpression);
            return this;
        }

        public Builder addNumberExpression(final Map<String, String> numberExpression) {
            numberExpression.forEach(this::addNumberExpression);
            return this;
        }

        public Builder addSqlFunction(final Map<String, String> sqlFunction) {
            sqlFunction.forEach(this::addSqlFunction);
            return this;
        }

        protected void addStringExpression(final String key, final String value) {
            this.stringExpression.put(key, value);
        }

        protected void addBooleanExpression(final String key, final String value) {
            this.booleanExpression.put(key, value);
        }

        protected void addNumberExpression(final String key, final String value) {
            this.numberExpression.put(key, value);
        }

        protected void addSqlFunction(final String key, final String value) {
            this.sqlFunction.put(key, value);
        }

        public Map<String, String> getStringExpression() {
            return this.stringExpression;
        }

        public Map<String, String> getBooleanExpression() {
            return this.booleanExpression;
        }

        public Map<String, String> getNumberExpression() {
            return this.numberExpression;
        }

        public Map<String, String> getSqlFunction() {
            return this.sqlFunction;
        }
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
}
