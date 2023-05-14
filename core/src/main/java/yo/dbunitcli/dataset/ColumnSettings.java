package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.filter.IColumnFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record ColumnSettings(AddSettingColumns comparisonKeys
        , AddSettingColumns excludeColumns
        , AddSettingColumns orderColumns
        , AddSettingColumns expressionColumns
        , TableSeparators tableSeparators
) {

    public static ColumnSettings NONE = new ColumnSettings(AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , TableSeparators.NONE);

    public ColumnSettings(final Builder builder) {
        this(builder.getComparisonKeys()
                , builder.getExcludeColumns()
                , builder.getOrderColumns()
                , builder.getExpressionColumns()
                , builder.getTableSeparators()
        );
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
                , this.tableSeparators.apply(editor.getSeparatorEdit())
        );
    }

    public ColumnSettings add(final ColumnSettings other) {
        return this.apply(editor -> editor.setKeyEdit(it -> it.add(other.comparisonKeys))
                .setExcludeEdit(it -> it.add(other.excludeColumns))
                .setOrderEdit(it -> it.add(other.orderColumns))
                .setExpressionEdit(it -> it.add(other.expressionColumns))
                .setSeparatorEdit(it -> it.add(other.tableSeparators))
        );
    }

    public ComparableTableMapper createMapper(final ITableMetaData metaData) {
        final List<AddSettingTableMetaData> results = this.getAddSettingTableMetaData(metaData);
        if (results.size() == 1) {
            return this.createMapperFrom(results.get(0));
        }
        return new ComparableTableMapperMulti(results.stream()
                .map(this::createMapperFrom)
                .collect(Collectors.toList()));
    }

    private ComparableTableMapper createMapperFrom(final AddSettingTableMetaData results) {
        return new ComparableTableMapperSingle(results, this.getOrderColumns(results.getTableName()));
    }

    private List<AddSettingTableMetaData> getAddSettingTableMetaData(final ITableMetaData metaData) {
        return this.addSetting(metaData).stream()
                .map(it -> {
                    ITableMetaData origin = metaData;
                    AddSettingTableMetaData resultMetaData = it;
                    while (!origin.getTableName().equals(resultMetaData.getTableName())) {
                        origin = resultMetaData;
                        final List<AddSettingTableMetaData> addSetting = this.addSetting(resultMetaData);
                        if (addSetting.size() == 1) {
                            resultMetaData = addSetting.get(0);
                        } else {
                            return addSetting.stream()
                                    .map(this::getAddSettingTableMetaData)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());
                        }
                    }
                    return Collections.singletonList(resultMetaData);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Column[] getComparisonKeys(final String tableName) {
        return this.getComparisonKeys()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    private List<Column> getExcludeColumns(final String tableName) {
        return this.getExcludeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    private Column[] getOrderColumns(final String tableName) {
        return this.getOrderColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    private ColumnExpression getExpressionColumns(final String tableName) {
        return this.getExpressionColumns().getExpression(tableName);
    }

    private IColumnFilter getExcludeColumnFilter(final String tableName) {
        final List<Column> columns = this.getExcludeColumns(tableName);
        if (columns.size() == 0) {
            return null;
        }
        final DefaultColumnFilter result = new DefaultColumnFilter();
        result.excludeColumns(columns.toArray(new Column[0]));
        return result;
    }

    private List<AddSettingTableMetaData> addSetting(final ITableMetaData originMetaData) {
        return this.getRowFilter(originMetaData.getTableName()).stream()
                .map(it -> this.addSetting(originMetaData, it))
                .collect(Collectors.toList());
    }

    private AddSettingTableMetaData addSetting(final ITableMetaData originMetaData, final TableSeparator tableSeparator) {
        return this.getExpressionColumns(originMetaData.getTableName())
                .apply(originMetaData
                        , this.getExcludeColumnFilter(originMetaData.getTableName())
                        , this.getComparisonKeys(originMetaData.getTableName())
                        , tableSeparator);
    }

    private Collection<TableSeparator> getRowFilter(final String tableName) {
        return this.tableSeparators.getSeparators(tableName);
    }

    public interface Builder {

        default ColumnSettings build(final String settings) throws IOException {
            if (Optional.ofNullable(settings).orElse("").isEmpty()) {
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

        TableSeparators getTableSeparators();

    }

}
