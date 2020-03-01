package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ColumnSettings {

    public static ColumnSettings NONE = new ColumnSettings(AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , RowFilter.NONE);

    private final AddSettingColumns comparisonKeys;

    private final AddSettingColumns excludeColumns;

    private final AddSettingColumns orderColumns;

    private final AddSettingColumns expressionColumns;

    private final RowFilter filterExpressions;

    public ColumnSettings(Builder builder) {
        this(builder.getComparisonKeys()
                , builder.getExcludeColumns()
                , builder.getOrderColumns()
                , builder.getExpressionColumns()
                , builder.getFilterExpressions()
        );
    }

    private ColumnSettings(AddSettingColumns comparisonKeys
            , AddSettingColumns excludeColumns
            , AddSettingColumns orderColumns
            , AddSettingColumns expressionColumns
            , RowFilter filterExpressions) {
        this.comparisonKeys = comparisonKeys;
        this.excludeColumns = excludeColumns;
        this.orderColumns = orderColumns;
        this.expressionColumns = expressionColumns;
        this.filterExpressions = filterExpressions;
    }

    public AddSettingColumns getComparisonKeys() {
        return comparisonKeys;
    }

    public AddSettingColumns getExcludeColumns() {
        return excludeColumns;
    }

    public AddSettingColumns getOrderColumns() {
        return orderColumns;
    }

    public AddSettingColumns getExpressionColumns() {
        return expressionColumns;
    }

    public Column[] getComparisonKeys(String tableName) {
        return this.getComparisonKeys()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    public List<Column> getExcludeColumns(String tableName) {
        return this.getExcludeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    public Column[] getOrderColumns(String tableName) {
        return this.getOrderColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    public ColumnExpression getExpressionColumns(String tableName) {
        return this.getExpressionColumns().getExpression(tableName);
    }

    public IColumnFilter getExcludeColumnFilter(String tableName) {
        List<Column> columns = this.getExcludeColumns(tableName);
        if (columns.size() == 0) {
            return null;
        }
        DefaultColumnFilter result = new DefaultColumnFilter();
        result.excludeColumns(columns.toArray(new Column[0]));
        return result;
    }

    public ComparableTable apply(ITable table) throws DataSetException {
        try {
            ITableMetaData originMetaData = table.getTableMetaData();
            AddSettingTableMetaData tableMetaData = this.getExpressionColumns(originMetaData.getTableName())
                    .apply(originMetaData, this.getExcludeColumnFilter(originMetaData.getTableName()));
            Column[] keyColumns = this.getComparisonKeys(originMetaData.getTableName());
            if (keyColumns.length > 0) {
                tableMetaData = tableMetaData.changePrimaryKey(keyColumns);
            }
            return new ComparableTable(tableMetaData
                    , this.getFilterColumnIndex(originMetaData, tableMetaData)
                    , this.getOriginRows(table)
                    , this.getComparator(table)
                    , this.getRowFilter(tableMetaData));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
    }

    private Predicate<Map<String, Object>> getRowFilter(AddSettingTableMetaData tableMetaData) {
        return this.filterExpressions.getRowFilter(tableMetaData.getTableName());
    }

    protected List<Integer> getFilterColumnIndex(ITableMetaData originMetaData, AddSettingTableMetaData tableMetaData) throws DataSetException {
        Set<Column> noFilter = Sets.newHashSet(originMetaData.getColumns());
        Set<Column> filtered = Sets.newHashSet(tableMetaData.getColumns());
        List<Integer> filterColumnIndex = Lists.newArrayList();
        for (Column column : Sets.difference(noFilter, filtered)) {
            filterColumnIndex.add(originMetaData.getColumnIndex(column.getColumnName()));
        }
        return filterColumnIndex;
    }

    protected List<Object[]> getOriginRows(ITable delegate) throws NoSuchFieldException, IllegalAccessException {
        Field f = delegate.getClass().getDeclaredField("_rowList");
        f.setAccessible(true);
        return (List<Object[]>) f.get(delegate);
    }

    protected Comparator<Object> getComparator(ITable delegate) {
        Column[] orderColumns = this.getOrderColumns(delegate.getTableMetaData().getTableName());
        if (orderColumns.length > 0) {
            return new SortedTable.AbstractRowComparator(delegate, orderColumns) {
                @Override
                protected int compare(Column column, Object o, Object o1) throws TypeCastException {
                    return column.getDataType().compare(o, o1);
                }
            };
        }
        return null;
    }

    public interface Builder {

        ColumnSettings build(File setting) throws IOException;

        default ColumnSettings build() {
            return new ColumnSettings(this);
        }

        AddSettingColumns getComparisonKeys();

        AddSettingColumns getExcludeColumns();

        AddSettingColumns getOrderColumns();

        AddSettingColumns getExpressionColumns();

        RowFilter getFilterExpressions();
    }
}
