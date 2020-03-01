package yo.dbunitcli.dataset;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.jexl3.*;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.IColumnFilter;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnExpression {

    private Map<String, String> stringExpression = Maps.newLinkedHashMap();

    private Map<String, String> booleanExpression = Maps.newLinkedHashMap();

    private Map<String, String> numberExpression = Maps.newLinkedHashMap();

    public ColumnExpression(Builder builder) {
        this.stringExpression.putAll(builder.stringExpression);
        this.booleanExpression.putAll(builder.booleanExpression);
        this.numberExpression.putAll(builder.numberExpression);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ColumnExpression add(ColumnExpression other) {
        return builder().add(this).add(other).build();
    }

    public AddExpressionTableMetaData apply(ITableMetaData originMetaData, IColumnFilter iColumnFilter) throws DataSetException {
        if (iColumnFilter == null) {
            return this.apply(originMetaData);
        }
        return this.apply(new FilteredTableMetaData(originMetaData, iColumnFilter));
    }

    public AddExpressionTableMetaData apply(ITableMetaData delegateMetaData) throws DataSetException {
        return new AddExpressionTableMetaData(delegateMetaData, this);
    }

    public Collection<? extends Column> getColumns() {
        Set<Column> result = Sets.newLinkedHashSet();
        this.stringExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NVARCHAR)));
        this.booleanExpression.keySet().forEach(key -> result.add(new Column(key, DataType.BOOLEAN)));
        this.numberExpression.keySet().forEach(key -> result.add(new Column(key, DataType.NUMERIC)));
        return result;
    }

    public Column[] merge(Column[] columns) {
        if (this.size() > 0) {
            ArrayList<Column> columnList = Lists.newArrayList(columns);
            List<String> columnNames = columnList.stream().map(Column::getColumnName).collect(Collectors.toList());
            for (Column column : this.getColumns()) {
                if (!columnNames.contains(column.getColumnName())) {
                    columnList.add(column);
                }
            }
            return columnList.toArray(new Column[0]);
        }
        return columns;
    }

    public int size() {
        return this.stringExpression.size() + this.booleanExpression.size() + this.numberExpression.size();
    }

    public boolean contains(String columnName) {
        return this.stringExpression.containsKey(columnName)
                || this.booleanExpression.containsKey(columnName)
                || this.numberExpression.containsKey(columnName);
    }

    public Object evaluate(String columnName, Map<String, Object> param) {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext jc = new MapContext(param);
        if (this.stringExpression.containsKey(columnName)) {
            return jexl.createExpression(this.stringExpression.get(columnName)).evaluate(jc);
        } else if (this.booleanExpression.containsKey(columnName)) {
            return Boolean.parseBoolean(jexl.createExpression(this.booleanExpression.get(columnName)).evaluate(jc).toString());
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
        private Map<String, String> stringExpression = Maps.newLinkedHashMap();

        private Map<String, String> booleanExpression = Maps.newLinkedHashMap();

        private Map<String, String> numberExpression = Maps.newLinkedHashMap();

        public Builder add(ColumnExpression columnExpression) {
            return this.addStringExpression(columnExpression.stringExpression)
                    .addBooleanExpression(columnExpression.booleanExpression)
                    .addNumberExpression(columnExpression.numberExpression);
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

        protected void addStringExpression(String key, String value) {
            this.stringExpression.put(key, value);
        }

        protected void addBooleanExpression(String key, String value) {
            this.booleanExpression.put(key, value);
        }

        protected void addNumberExpression(String key, String value) {
            this.numberExpression.put(key, value);
        }

    }

    public enum ParameterType {
        STRING, BOOLEAN, NUMBER;

        public String keyName() {
            return this.name().toLowerCase() + "Expression";
        }
    }
}
