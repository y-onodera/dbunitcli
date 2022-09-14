package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ColumnSettings {

    public static ColumnSettings NONE = new ColumnSettings(it -> it, AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , RowFilter.NONE);

    private final Function<String, String> tableNameMap;

    private final AddSettingColumns comparisonKeys;

    private final AddSettingColumns excludeColumns;

    private final AddSettingColumns orderColumns;

    private final AddSettingColumns expressionColumns;

    private final RowFilter filterExpressions;

    public ColumnSettings(Builder builder) {
        this(builder.getTableNameMap(), builder.getComparisonKeys()
                , builder.getExcludeColumns()
                , builder.getOrderColumns()
                , builder.getExpressionColumns()
                , builder.getFilterExpressions()
        );
    }

    private ColumnSettings(Function<String, String> tableNameMap, AddSettingColumns comparisonKeys
            , AddSettingColumns excludeColumns
            , AddSettingColumns orderColumns
            , AddSettingColumns expressionColumns
            , RowFilter filterExpressions) {
        this.tableNameMap = tableNameMap;
        this.comparisonKeys = comparisonKeys;
        this.excludeColumns = excludeColumns;
        this.orderColumns = orderColumns;
        this.expressionColumns = expressionColumns;
        this.filterExpressions = filterExpressions;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.comparisonKeys;
    }

    public AddSettingColumns getExcludeColumns() {
        return this.excludeColumns;
    }

    public AddSettingColumns getOrderColumns() {
        return this.orderColumns;
    }

    public AddSettingColumns getExpressionColumns() {
        return this.expressionColumns;
    }

    public ColumnSettings apply(Consumer<ColumnSettings.Editor> function) {
        Editor editor = new Editor();
        function.accept(editor);
        return new ColumnSettings(editor.tableNameMapEdit.apply(this.tableNameMap)
                , this.comparisonKeys.apply(editor.keyEdit)
                , this.excludeColumns.apply(editor.excludeEdit)
                , this.orderColumns.apply(editor.orderEdit)
                , this.expressionColumns.apply(editor.expressionEdit)
                , this.filterExpressions.apply(editor.filterEdit));
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
            ComparableTable result = this.toComparableTable(table);
            if (!table.getTableMetaData().getTableName().equals(result.getTableMetaData().getTableName())) {
                return this.apply(result);
            }
            return result;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
    }

    protected ComparableTable toComparableTable(ITable table) throws DataSetException, NoSuchFieldException, IllegalAccessException {
        return new ComparableTable(this.getAddSettingTableMetaData(table.getTableMetaData())
                , this.getOriginRows(table)
                , this.getComparator(table.getTableMetaData().getTableName())
                , this.getRowFilter(table.getTableMetaData().getTableName()));
    }

    protected AddSettingTableMetaData getAddSettingTableMetaData(ITableMetaData originMetaData) throws DataSetException {
        return this.getExpressionColumns(originMetaData.getTableName())
                .apply(this.getTableName(originMetaData)
                        , originMetaData
                        , this.getExcludeColumnFilter(originMetaData.getTableName())
                        , this.getComparisonKeys(originMetaData.getTableName()));
    }

    protected String getTableName(ITableMetaData originMetaData) {
        return tableNameMap.apply(originMetaData.getTableName());
    }

    protected List<Object[]> getOriginRows(ITable delegate) throws NoSuchFieldException, IllegalAccessException, RowOutOfBoundsException {
        if (delegate instanceof ComparableTable) {
            return ((ComparableTable) delegate).getRows();
        }
        Field f = delegate.getClass().getDeclaredField("_rowList");
        f.setAccessible(true);
        return (List<Object[]>) f.get(delegate);
    }

    protected Column[] getComparator(String tableName) {
        return this.getOrderColumns(tableName);
    }

    protected Predicate<Map<String, Object>> getRowFilter(String tableName) {
        return this.filterExpressions.getRowFilter(tableName);
    }

    public interface Builder {

        ColumnSettings build(File setting) throws IOException;

        default ColumnSettings build() {
            return new ColumnSettings(this);
        }

        Function<String, String> getTableNameMap();

        AddSettingColumns getComparisonKeys();

        AddSettingColumns getExcludeColumns();

        AddSettingColumns getOrderColumns();

        AddSettingColumns getExpressionColumns();

        RowFilter getFilterExpressions();
    }

    public static class Editor {
        private Function<Function<String, String>, Function<String, String>> tableNameMapEdit = it -> it;
        private Consumer<AddSettingColumns.Builder> keyEdit = (it) -> {
        };
        private Consumer<AddSettingColumns.Builder> excludeEdit = (it) -> {
        };
        private Consumer<AddSettingColumns.Builder> orderEdit = (it) -> {
        };
        private Consumer<AddSettingColumns.Builder> expressionEdit = (it) -> {
        };
        private Consumer<RowFilter.Builder> filterEdit = (it) -> {
        };

        public Editor setTableNameMapEdit(Function<Function<String, String>, Function<String, String>> function) {
            this.tableNameMapEdit = function;
            return this;
        }

        public Editor setKeyEdit(Consumer<AddSettingColumns.Builder> key) {
            this.keyEdit = key;
            return this;
        }

        public Editor setExcludeEdit(Consumer<AddSettingColumns.Builder> exclude) {
            this.excludeEdit = exclude;
            return this;
        }

        public Editor setOrderEdit(Consumer<AddSettingColumns.Builder> order) {
            this.orderEdit = order;
            return this;
        }

        public Editor setExpressionEdit(Consumer<AddSettingColumns.Builder> expression) {
            this.expressionEdit = expression;
            return this;
        }

        public Editor setFilterEdit(Consumer<RowFilter.Builder> filter) {
            this.filterEdit = filter;
            return this;
        }
    }
}
