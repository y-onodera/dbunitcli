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

    public ColumnExpression(Builder builder) {
        this.stringExpression.putAll(builder.stringExpression);
        this.booleanExpression.putAll(builder.booleanExpression);
        this.numberExpression.putAll(builder.numberExpression);
        this.sqlFunction.putAll(builder.sqlFunction);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ColumnExpression add(ColumnExpression other) {
        return builder().add(this).add(other).build();
    }

    public AddSettingTableMetaData apply(ITableMetaData delegateMetaData) throws DataSetException {
        return this.apply(delegateMetaData.getTableName(), delegateMetaData, null, delegateMetaData.getPrimaryKeys(), null);
    }

    public AddSettingTableMetaData apply(String tableName, ITableMetaData originMetaData, IColumnFilter iColumnFilter, Column[] comparisonKeys, Predicate<Map<String, Object>> rowFilter) throws DataSetException {
        Column[] primaryKey = originMetaData.getPrimaryKeys();
        if (comparisonKeys.length > 0) {
            primaryKey = comparisonKeys;
        }
        return new AddSettingTableMetaData(tableName, originMetaData, primaryKey, iColumnFilter, rowFilter, this);
    }

    public Collection<? extends Column> getColumns() {
        Set<Column> result = Sets.newLinkedHashSet();
        this.stringExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NVARCHAR)));
        this.booleanExpression.keySet().forEach(key -> result.add(new Column(key, DataType.BOOLEAN)));
        this.numberExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        this.sqlFunction.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        return result;
    }

    public Column[] merge(Column[] columns) {
        if (this.size() > 0) {
            ArrayList<Column> columnList = Lists.newArrayList(columns);
            List<String> columnNames = columnList.stream().map(Column::getColumnName).collect(Collectors.toList());
            for (Column column : this.getColumns()) {
                if (!columnNames.contains(column.getColumnName())) {
                    columnList.add(column);
                } else {
                    columnList.replaceAll(it -> it.getColumnName().equals(column.getColumnName()) ? column : it);
                }
            }
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

    public boolean contains(String columnName) {
        return this.stringExpression.containsKey(columnName)
                || this.booleanExpression.containsKey(columnName)
                || this.numberExpression.containsKey(columnName)
                || this.sqlFunction.containsKey(columnName)
                ;
    }

    public Object evaluate(String columnName, Map<String, Object> param) {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext jc = new MapContext(param);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnExpression that = (ColumnExpression) o;
        return Objects.equal(stringExpression, that.stringExpression) &&
                Objects.equal(booleanExpression, that.booleanExpression) &&
                Objects.equal(numberExpression, that.numberExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stringExpression, booleanExpression, numberExpression);
    }

    @Override
    public String toString() {
        return "ColumnExpression{" +
                "stringExpression=" + stringExpression +
                ", booleanExpression=" + booleanExpression +
                ", numberExpression=" + numberExpression +
                '}';
    }

    public static class Builder {
        private final Map<String, String> stringExpression = Maps.newLinkedHashMap();

        private final Map<String, String> booleanExpression = Maps.newLinkedHashMap();

        private final Map<String, String> numberExpression = Maps.newLinkedHashMap();

        private final Map<String, String> sqlFunction = Maps.newLinkedHashMap();

        public Builder add(ColumnExpression columnExpression) {
            return this.addStringExpression(columnExpression.stringExpression)
                    .addBooleanExpression(columnExpression.booleanExpression)
                    .addNumberExpression(columnExpression.numberExpression)
                    .addSqlFunction(columnExpression.sqlFunction);
        }

        public ColumnExpression build() {
            return new ColumnExpression(this);
        }

        public void addExpression(ParameterType type, String key, String value) {
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

        public Builder addStringExpression(Map<String, String> stringExpression) {
            stringExpression.forEach(this::addStringExpression);
            return this;
        }

        public Builder addBooleanExpression(Map<String, String> booleanExpression) {
            booleanExpression.forEach(this::addBooleanExpression);
            return this;
        }

        public Builder addNumberExpression(Map<String, String> numberExpression) {
            numberExpression.forEach(this::addNumberExpression);
            return this;
        }

        public Builder addSqlFunction(Map<String, String> sqlFunction) {
            sqlFunction.forEach(this::addSqlFunction);
            return this;
        }

        protected void addStringExpression(String key, String value) {
            this.stringExpression.put(key, value);
        }

        protected void addBooleanExpression(String key, String value) {
            this.booleanExpression.put(key, value);
        }

        protected void addNumberExpression(String key, String value) {
            this.numberExpression.put(key, value);
        }

        protected void addSqlFunction(String key, String value) {
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
