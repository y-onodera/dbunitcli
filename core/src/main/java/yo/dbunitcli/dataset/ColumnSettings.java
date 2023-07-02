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
        , AddSettingColumns includeColumns
        , AddSettingColumns excludeColumns
        , AddSettingColumns orderColumns
        , AddSettingColumns expressionColumns
        , TableSeparators tableSeparators
        , AddSettingColumns distinct
) {

    public static ColumnSettings NONE = new ColumnSettings(AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , AddSettingColumns.NONE
            , TableSeparators.NONE
            , AddSettingColumns.NONE
    );

    public ColumnSettings(final Builder builder) {
        this(builder.getComparisonKeys()
                , builder.getIncludeColumns()
                , builder.getExcludeColumns()
                , builder.getOrderColumns()
                , builder.getExpressionColumns()
                , builder.getTableSeparators()
                , builder.getDistinct()
        );
    }

    public ColumnSettings apply(final Consumer<ColumnSettingEditor> function) {
        final ColumnSettingEditor editor = new ColumnSettingEditor();
        function.accept(editor);
        return new ColumnSettings(this.comparisonKeys.apply(editor.getKeyEdit())
                , this.includeColumns.apply(editor.getIncludeEdit())
                , this.excludeColumns.apply(editor.getExcludeEdit())
                , this.orderColumns.apply(editor.getOrderEdit())
                , this.expressionColumns.apply(editor.getExpressionEdit())
                , this.tableSeparators.apply(editor.getSeparatorEdit())
                , this.distinct.apply(editor.getDistinctEdit())
        );
    }

    public ColumnSettings add(final ColumnSettings other) {
        return this.apply(editor -> editor.setKeyEdit(it -> it.add(other.comparisonKeys))
                .setIncludeEdit(it -> it.add(other.includeColumns))
                .setExcludeEdit(it -> it.add(other.excludeColumns))
                .setOrderEdit(it -> it.add(other.orderColumns))
                .setExpressionEdit(it -> it.add(other.expressionColumns))
                .setSeparatorEdit(it -> it.add(other.tableSeparators))
                .setDistinctEdit(it -> it.add(other.distinct))
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

    private List<AddSettingTableMetaData> addSetting(final ITableMetaData originMetaData) {
        return this.getRowFilter(originMetaData.getTableName()).stream()
                .map(it -> this.addSetting(originMetaData, it))
                .collect(Collectors.toList());
    }

    private AddSettingTableMetaData addSetting(final ITableMetaData originMetaData, final TableSeparator tableSeparator) {
        return this.getExpressionColumns(originMetaData.getTableName())
                .apply(originMetaData
                        , this.getColumnFilter(originMetaData.getTableName())
                        , this.getComparisonKeys(originMetaData.getTableName())
                        , tableSeparator
                        , this.isDistinct(originMetaData.getTableName()));
    }

    private Column[] getComparisonKeys(final String tableName) {
        return this.comparisonKeys()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    private List<Column> getIncludeColumns(final String tableName) {
        return this.includeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    private List<Column> getExcludeColumns(final String tableName) {
        return this.excludeColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .collect(Collectors.toList());
    }

    private Column[] getOrderColumns(final String tableName) {
        return this.orderColumns()
                .getColumns(tableName)
                .stream()
                .map(it -> new Column(it, DataType.UNKNOWN))
                .toArray(Column[]::new);
    }

    private ColumnExpression getExpressionColumns(final String tableName) {
        return this.expressionColumns().getExpression(tableName);
    }

    private IColumnFilter getColumnFilter(final String tableName) {
        final List<Column> includeColumns = this.getIncludeColumns(tableName);
        final List<Column> excludeColumns = this.getExcludeColumns(tableName);
        if (excludeColumns.size() == 0 && includeColumns.size() == 0) {
            return null;
        }
        final DefaultColumnFilter result = new DefaultColumnFilter();
        if (includeColumns.size() > 0) {
            result.includeColumns(includeColumns.toArray(new Column[0]));
        }
        if (excludeColumns.size() > 0) {
            result.excludeColumns(excludeColumns.toArray(new Column[0]));
        }
        return result;
    }

    private Boolean isDistinct(final String tableName) {
        return this.distinct().getFlg(tableName);
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

        AddSettingColumns getIncludeColumns();

        AddSettingColumns getExcludeColumns();

        AddSettingColumns getOrderColumns();

        AddSettingColumns getExpressionColumns();

        TableSeparators getTableSeparators();

        AddSettingColumns getDistinct();
    }

}
