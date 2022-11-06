package yo.dbunitcli.dataset;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ColumnExpression {

    private final Map<String, String> stringExpression = Maps.newLinkedHashMap();

    private final Map<String, String> booleanExpression = Maps.newLinkedHashMap();

    private final Map<String, String> numberExpression = Maps.newLinkedHashMap();

    private final Map<String, String> sqlFunction = Maps.newLinkedHashMap();

    public ColumnExpression(final Builder builder) {
        this.stringExpression.putAll(builder.stringExpression);
        this.booleanExpression.putAll(builder.booleanExpression);
        this.numberExpression.putAll(builder.numberExpression);
        this.sqlFunction.putAll(builder.sqlFunction);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ColumnExpression add(final ColumnExpression other) {
        return builder().add(this).add(other).build();
    }

    public AddSettingTableMetaData apply(final ITableMetaData delegateMetaData) throws DataSetException {
        return this.apply(delegateMetaData.getTableName(), delegateMetaData, null, delegateMetaData.getPrimaryKeys(), null);
    }

    public AddSettingTableMetaData apply(final String tableName, final ITableMetaData originMetaData, final IColumnFilter iColumnFilter, final Column[] comparisonKeys, final Predicate<Map<String, Object>> rowFilter) throws DataSetException {
        Column[] primaryKey = originMetaData.getPrimaryKeys();
        if (comparisonKeys.length > 0) {
            primaryKey = comparisonKeys;
        }
        return new AddSettingTableMetaData(tableName, originMetaData, primaryKey, iColumnFilter, rowFilter, this);
    }

    public Collection<? extends Column> getColumns() {
        final Set<Column> result = Sets.newLinkedHashSet();
        this.stringExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NVARCHAR)));
        this.booleanExpression.keySet().forEach(key -> result.add(new Column(key, DataType.BOOLEAN)));
        this.numberExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        this.sqlFunction.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        return result;
    }

    public Column[] merge(final Column[] columns) {
        if (this.size() > 0) {
            final ArrayList<Column> columnList = Lists.newArrayList(columns);
            final List<String> columnNames = columnList.stream().map(Column::getColumnName).collect(Collectors.toList());
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ColumnExpression that = (ColumnExpression) o;
        return Objects.equal(this.stringExpression, that.stringExpression) &&
                Objects.equal(this.booleanExpression, that.booleanExpression) &&
                Objects.equal(this.numberExpression, that.numberExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.stringExpression, this.booleanExpression, this.numberExpression);
    }

    @Override
    public String toString() {
        return "ColumnExpression{" +
                "stringExpression=" + this.stringExpression +
                ", booleanExpression=" + this.booleanExpression +
                ", numberExpression=" + this.numberExpression +
                '}';
    }

    public static class Builder {
        private final Map<String, String> stringExpression = Maps.newLinkedHashMap();

        private final Map<String, String> booleanExpression = Maps.newLinkedHashMap();

        private final Map<String, String> numberExpression = Maps.newLinkedHashMap();

        private final Map<String, String> sqlFunction = Maps.newLinkedHashMap();

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
