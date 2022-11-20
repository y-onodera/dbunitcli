package yo.dbunitcli.dataset;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColumnSettings {

    public static ColumnSettings NONE = new ColumnSettings(AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , RowFilters.NONE
            , TableSplitter.NONE);

    private final AddSettingColumns comparisonKeys;

    private final AddSettingColumns excludeColumns;

    private final AddSettingColumns orderColumns;

    private final AddSettingColumns expressionColumns;

    private final RowFilters rowFilters;

    private final TableSplitter tableSplitters;

    public ColumnSettings(final Builder builder) {
        this(builder.getComparisonKeys()
                , builder.getExcludeColumns()
                , builder.getOrderColumns()
                , builder.getExpressionColumns()
                , builder.getRowFilters()
                , builder.getTableSplitters()
        );
    }

    private ColumnSettings(final AddSettingColumns comparisonKeys
            , final AddSettingColumns excludeColumns
            , final AddSettingColumns orderColumns
            , final AddSettingColumns expressionColumns
            , final RowFilters rowFilters
            , final TableSplitter tableSplitters) {
        this.comparisonKeys = comparisonKeys;
        this.excludeColumns = excludeColumns;
        this.orderColumns = orderColumns;
        this.expressionColumns = expressionColumns;
        this.rowFilters = rowFilters;
        this.tableSplitters = tableSplitters;
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

    public ColumnSettings apply(final Consumer<ColumnSettingEditor> function) {
        final ColumnSettingEditor editor = new ColumnSettingEditor();
        function.accept(editor);
        return new ColumnSettings(this.comparisonKeys.apply(editor.getKeyEdit())
                , this.excludeColumns.apply(editor.getExcludeEdit())
                , this.orderColumns.apply(editor.getOrderEdit())
                , this.expressionColumns.apply(editor.getExpressionEdit())
                , this.rowFilters.apply(editor.getFilterEdit()).editCommonRenameFunction(editor.getTableRenameFunctionEdit())
                , this.tableSplitters.apply(editor.getTableSplitterEdit()));
    }

    public ColumnSettings add(final ColumnSettings other) {
        return this.apply(editor -> editor.setKeyEdit(it -> it.add(other.comparisonKeys))
                .setExcludeEdit(it -> it.add(other.excludeColumns))
                .setOrderEdit(it -> it.add(other.orderColumns))
                .setExpressionEdit(it -> it.add(other.expressionColumns))
                .setFilterEdit(it -> it.add(other.rowFilters))
                .setGetTableSplitterEdit(it -> it.add(other.tableSplitters))
        );
    }

    public ComparableTableMapper createMapper(ITableMetaData metaData) {
        final List<AddSettingTableMetaData> settings = Lists.newArrayList();
        AddSettingTableMetaData resultMetaData = this.addSetting(metaData);
        while (!metaData.getTableName().equals(resultMetaData.getTableName())) {
            settings.add(resultMetaData);
            metaData = resultMetaData;
            resultMetaData = this.addSetting(resultMetaData);
        }
        return new ComparableTableMapper(resultMetaData, this.getOrderColumns(resultMetaData.getTableName()), settings);
    }

    protected Column[] getComparisonKeys(final String tableName) {
        return this.getComparisonKeys()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    protected List<Column> getExcludeColumns(final String tableName) {
        return this.getExcludeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    protected Column[] getOrderColumns(final String tableName) {
        return this.getOrderColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    protected ColumnExpression getExpressionColumns(final String tableName) {
        return this.getExpressionColumns().getExpression(tableName);
    }

    protected IColumnFilter getExcludeColumnFilter(final String tableName) {
        final List<Column> columns = this.getExcludeColumns(tableName);
        if (columns.size() == 0) {
            return null;
        }
        final DefaultColumnFilter result = new DefaultColumnFilter();
        result.excludeColumns(columns.toArray(new Column[0]));
        return result;
    }

    protected AddSettingTableMetaData addSetting(final ITableMetaData originMetaData) {
        return this.getExpressionColumns(originMetaData.getTableName())
                .apply(originMetaData
                        , this.getExcludeColumnFilter(originMetaData.getTableName())
                        , this.getComparisonKeys(originMetaData.getTableName())
                        , this.getRowFilter(originMetaData.getTableName()));
    }

    protected RowFilter getRowFilter(final String tableName) {
        return this.rowFilters.getRowFilter(tableName);
    }

    @Override
    public String toString() {
        return "ColumnSettings{" +
                " comparisonKeys=" + this.comparisonKeys +
                ", excludeColumns=" + this.excludeColumns +
                ", orderColumns=" + this.orderColumns +
                ", expressionColumns=" + this.expressionColumns +
                ", filterExpressions=" + this.rowFilters +
                '}';
    }

    public interface Builder {

        default ColumnSettings build(final String settings) throws IOException {
            if (Strings.isNullOrEmpty(settings)) {
                return this.build((File) null);
            } else {
                return this.build(Arrays.stream(settings.split(","))
                        .map(File::new)
                        .toArray(File[]::new));
            }
        }

        default ColumnSettings build(final File... settings) {
            ColumnSettings result = ColumnSettings.NONE;
            for (final File setting : settings) {
                result = result.add(this.build(setting));
            }
            return result;
        }

        ColumnSettings build(File setting);

        default ColumnSettings build() {
            return new ColumnSettings(this);
        }

        AddSettingColumns getComparisonKeys();

        AddSettingColumns getExcludeColumns();

        AddSettingColumns getOrderColumns();

        AddSettingColumns getExpressionColumns();

        RowFilters getRowFilters();

        TableSplitter getTableSplitters();
    }

}
