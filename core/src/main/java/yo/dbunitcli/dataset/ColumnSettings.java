package yo.dbunitcli.dataset;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

    public ColumnSettings apply(Consumer<ColumnSettingEditor> function) {
        ColumnSettingEditor editor = new ColumnSettingEditor();
        function.accept(editor);
        return new ColumnSettings(editor.getTableNameMapEdit().apply(this.tableNameMap)
                , this.comparisonKeys.apply(editor.getKeyEdit())
                , this.excludeColumns.apply(editor.getExcludeEdit())
                , this.orderColumns.apply(editor.getOrderEdit())
                , this.expressionColumns.apply(editor.getExpressionEdit())
                , this.filterExpressions.apply(editor.getFilterEdit()));
    }

    public ColumnSettings add(ColumnSettings other) {
        return this.apply(editor -> editor.setTableNameMapEdit(other.tableNameMap::andThen)
                .setKeyEdit(it -> it.add(other.comparisonKeys))
                .setExcludeEdit(it -> it.add(other.excludeColumns))
                .setOrderEdit(it -> it.add(other.orderColumns))
                .setExpressionEdit(it -> it.add(other.expressionColumns))
                .setFilterEdit(it -> it.add(other.filterExpressions)));
    }

    public ComparableTableMapper createMapper(ITableMetaData metaData) throws DataSetException {
        List<AddSettingTableMetaData> settings = Lists.newArrayList();
        AddSettingTableMetaData resultMetaData = this.addSetting(metaData);
        while (!metaData.getTableName().equals(resultMetaData.getTableName())) {
            settings.add(resultMetaData);
            metaData = resultMetaData;
            resultMetaData = this.addSetting(resultMetaData);
        }
        return new ComparableTableMapper(resultMetaData, this.getOrderColumns(resultMetaData.getTableName()), settings);
    }

    protected Column[] getComparisonKeys(String tableName) {
        return this.getComparisonKeys()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    protected List<Column> getExcludeColumns(String tableName) {
        return this.getExcludeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    protected Column[] getOrderColumns(String tableName) {
        return this.getOrderColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    protected ColumnExpression getExpressionColumns(String tableName) {
        return this.getExpressionColumns().getExpression(tableName);
    }

    protected IColumnFilter getExcludeColumnFilter(String tableName) {
        List<Column> columns = this.getExcludeColumns(tableName);
        if (columns.size() == 0) {
            return null;
        }
        DefaultColumnFilter result = new DefaultColumnFilter();
        result.excludeColumns(columns.toArray(new Column[0]));
        return result;
    }

    protected AddSettingTableMetaData addSetting(ITableMetaData originMetaData) throws DataSetException {
        return this.getExpressionColumns(originMetaData.getTableName())
                .apply(this.getTableName(originMetaData)
                        , originMetaData
                        , this.getExcludeColumnFilter(originMetaData.getTableName())
                        , this.getComparisonKeys(originMetaData.getTableName())
                        , this.getRowFilter(originMetaData.getTableName()));
    }

    protected String getTableName(ITableMetaData originMetaData) {
        return tableNameMap.apply(originMetaData.getTableName());
    }

    protected Predicate<Map<String, Object>> getRowFilter(String tableName) {
        return this.filterExpressions.getRowFilter(tableName);
    }

    @Override
    public String toString() {
        return "ColumnSettings{" +
                "tableNameMap=" + tableNameMap +
                ", comparisonKeys=" + comparisonKeys +
                ", excludeColumns=" + excludeColumns +
                ", orderColumns=" + orderColumns +
                ", expressionColumns=" + expressionColumns +
                ", filterExpressions=" + filterExpressions +
                '}';
    }

    public interface Builder {

        default ColumnSettings build(String settings) throws IOException {
            if (Strings.isNullOrEmpty(settings)) {
                return this.build((File) null);
            } else {
                return this.build(Arrays.stream(settings.split(","))
                        .map(File::new)
                        .toArray(File[]::new));
            }
        }

        default ColumnSettings build(File... settings) throws IOException {
            ColumnSettings result = ColumnSettings.NONE;
            for (File setting : settings) {
                result = result.add(this.build(setting));
            }
            return result;
        }

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

}
