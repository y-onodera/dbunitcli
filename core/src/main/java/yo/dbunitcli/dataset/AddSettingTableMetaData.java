package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.NoSuchColumnException;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record AddSettingTableMetaData(
        String tableName,
        Column[] primaryKeys,
        Column[] columns,
        Map<String, Integer> columnIndexes,
        Column[] allColumns,
        ExpressionColumns additionalExpression,
        List<Integer> filteredColumnIndex,
        TableSeparator tableSeparator,
        Boolean distinct,
        AddSettingTableMetaData preset) implements ITableMetaData {

    public static Builder builder(final ITableMetaData originMetaData, final Column[] primaryKey, final TableSeparator tableSeparator) {
        return new Builder(originMetaData, primaryKey, tableSeparator);
    }

    public AddSettingTableMetaData(final Builder builder) {
        this(builder.getTableName()
                , builder.getPrimaryKeys()
                , builder.getColumns()
                , builder.getColumnIndexes()
                , builder.getAllColumns()
                , builder.getAdditionalExpression()
                , builder.getFilteredColumnIndex()
                , builder.getTableSeparator()
                , builder.getDistinct()
                , builder.getPreset()
        );
    }

    public AddSettingTableMetaData rename(final String newTableName) {
        return new AddSettingTableMetaData(newTableName
                , this.primaryKeys
                , this.columns
                , this.columnIndexes
                , this.allColumns
                , this.additionalExpression
                , this.filteredColumnIndex
                , this.tableSeparator
                , this.distinct
                , this.preset
        );
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public Column[] getColumns() {
        return this.columns;
    }

    @Override
    public Column[] getPrimaryKeys() {
        return this.primaryKeys;
    }

    @Override
    public int getColumnIndex(final String columnName) throws DataSetException {
        final Integer result = this.columnIndexes.get(columnName.toUpperCase());
        if (result != null) {
            return result;
        } else {
            throw new NoSuchColumnException(this.getTableName(), columnName.toUpperCase(),
                    " (Non-uppercase input column: " + columnName + ") in ColumnNameToIndexes cache map. " +
                            "Note that the map's column names are NOT case sensitive.");
        }
    }

    public Boolean isNeedDistinct() {
        return this.distinct || this.isPresetNeedDistinct();
    }

    public Object[] applySetting(final Object[] values) {
        return this.applySetting(values, this.preset != null);
    }

    public Map<String, Object> rowToMap(final Object[] row) {
        final Map<String, Object> map = new LinkedHashMap<>();
        IntStream.range(0, row.length).forEach(i -> map.put(this.columns[i].getColumnName(), row[i]));
        return map;
    }

    public Rows distinct(final Collection<Object[]> values, final Collection<Integer> filteredRowIndexes) {
        Rows target = new Rows(values, filteredRowIndexes);
        if (this.isPresetNeedDistinct()) {
            target = this.preset.distinct(values, filteredRowIndexes);
        }
        if (!this.distinct) {
            return target;
        }
        return target.distinct(row -> this.applySetting(row, this.preset != null && !this.preset.isNeedDistinct()));
    }

    public boolean hasRowFilter() {
        return this.tableSeparator.hasRowFilter();
    }

    public TableSplitter getTableSplitter() {
        return this.tableSeparator.splitter();
    }

    public List<String> getBreakKeys(final Object[] applySetting) {
        return this.tableSeparator.splitter().breakKeys().stream()
                .map(name -> {
                    try {
                        return this.getColumnIndex(name);
                    } catch (final DataSetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(index -> applySetting[index].toString())
                .toList();
    }

    public Column[] getOrderColumns() {
        return this.tableSeparator.getOrderColumns();
    }

    public boolean isSorted() {
        return this.getOrderColumns().length > 0;
    }

    private boolean isPresetNeedDistinct() {
        return this.preset != null && this.preset.isNeedDistinct();
    }

    private Object[] applySetting(final Object[] values, final boolean applyPreset) {
        Object[] applySettings = values;
        if (applyPreset) {
            applySettings = this.preset.applySetting(applySettings);
            if (applySettings == null) {
                return null;
            }
        }
        final Object[] result = this.filterColumn(this.applyExpression(applySettings));
        if (!this.tableSeparator.test(this.rowToMap(result))) {
            return null;
        }
        return result;
    }

    private Object[] applyExpression(final Object[] objects) {
        if (this.additionalExpression.size() == 0) {
            return objects;
        }
        final Map<String, Object> param = new HashMap<>();
        IntStream.range(0, objects.length).forEach(i -> param.put(this.allColumns[i].getColumnName(), objects[i]));
        final Object[] result = Arrays.copyOf(objects, this.allColumns.length);
        IntStream.range(0, result.length).forEach(i -> {
            final String columnName = this.allColumns[i].getColumnName();
            if (this.additionalExpression.contains(columnName)) {
                result[i] = this.additionalExpression.evaluate(columnName, param);
                param.put(columnName, result[i]);
            }
        });
        return result;
    }

    private Object[] filterColumn(final Object[] noFilter) {
        if (this.filteredColumnIndex.isEmpty()) {
            return noFilter;
        }
        return this.filteredColumnIndex.stream()
                .map(i -> noFilter[i])
                .toArray(Object[]::new);
    }

    public record Rows(List<Object[]> rows, List<Integer> filteredRowIndexes) {

        private static final BiPredicate<List<Object[]>, Object[]> DISTINCT_PREDICATE = (newRows, row) -> newRows.stream().noneMatch(it -> Arrays.equals(it, row));

        public Rows() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        public Rows(final Collection<Object[]> rows, final Collection<Integer> filteredRowIndexes) {
            this(new ArrayList<>(rows), new ArrayList<>(filteredRowIndexes));
        }

        public Object[] get(final int index) {
            return this.rows().get(index);
        }

        public Stream<Object[]> stream() {
            return this.rows.stream();
        }

        public int size() {
            return this.rows().size();
        }

        public boolean isFiltered() {
            return !this.filteredRowIndexes().isEmpty();
        }

        public Rows add(final Rows other) {
            final List<Object[]> mergeRows = new ArrayList<>(this.rows());
            final List<Integer> mergeFilteredRowIndexes = new ArrayList<>(this.filteredRowIndexes());
            mergeRows.addAll(other.rows());
            mergeFilteredRowIndexes.addAll(other.filteredRowIndexes());
            return new Rows(mergeRows, mergeFilteredRowIndexes);
        }

        public Rows map(final UnaryOperator<Object[]> rowFunction, final BiPredicate<List<Object[]>, Object[]> reduceCondition) {
            final List<Object[]> newRows = new ArrayList<>();
            final List<Integer> newFilteredRowIndexes = new ArrayList<>();
            for (int i = 0, j = this.rows.size(); i < j; i++) {
                final Object[] row = rowFunction.apply(this.rows.get(i));
                if (row != null && reduceCondition.test(newRows, row)) {
                    newRows.add(row);
                    if (this.isFiltered()) {
                        newFilteredRowIndexes.add(this.filteredRowIndexes.get(i));
                    }
                }
            }
            return new Rows(newRows, newFilteredRowIndexes);
        }

        public Rows distinct() {
            return this.map(it -> it, DISTINCT_PREDICATE);
        }

        public Rows distinct(final UnaryOperator<Object[]> rowFunction) {
            return this.map(rowFunction, DISTINCT_PREDICATE);
        }
    }

    public static class Builder {
        private final String tableName;
        private final Column[] primaryKeys;
        private final Column[] columns;
        private final Map<String, Integer> columnIndexes;
        private final Column[] allColumns;
        private final ExpressionColumns additionalExpression;
        private final List<Integer> filteredColumnIndex;
        private final TableSeparator tableSeparator;
        private final Boolean distinct;
        private AddSettingTableMetaData preset;

        public Builder(final ITableMetaData originMetaData, final Column[] primaryKeys, final TableSeparator tableSeparator) {
            this.tableSeparator = tableSeparator;
            this.tableName = this.tableSeparator.rename(originMetaData.getTableName());
            this.primaryKeys = primaryKeys;
            this.additionalExpression = this.tableSeparator.expressionColumns();
            this.allColumns = this.getAllColumns(originMetaData);
            this.columns = this.tableSeparator.filteredColumns(this.allColumns, originMetaData);
            this.filteredColumnIndex = this.createFilteredColumnIndex();
            if (originMetaData instanceof final AddSettingTableMetaData delegate) {
                this.preset = delegate;
            }
            this.distinct = this.tableSeparator.distinct();
            this.columnIndexes = this.createColumnIndexes();
        }

        public AddSettingTableMetaData build() {
            return new AddSettingTableMetaData(this);
        }

        public String getTableName() {
            return this.tableName;
        }

        public Column[] getPrimaryKeys() {
            return this.primaryKeys;
        }

        public Column[] getColumns() {
            return this.columns;
        }

        public Map<String, Integer> getColumnIndexes() {
            return this.columnIndexes;
        }

        public Column[] getAllColumns() {
            return this.allColumns;
        }

        public ExpressionColumns getAdditionalExpression() {
            return this.additionalExpression;
        }

        public List<Integer> getFilteredColumnIndex() {
            return this.filteredColumnIndex;
        }

        public TableSeparator getTableSeparator() {
            return this.tableSeparator;
        }

        public Boolean getDistinct() {
            return this.distinct;
        }

        public AddSettingTableMetaData getPreset() {
            return this.preset;
        }

        private Column[] getAllColumns(final ITableMetaData delegate) {
            try {
                return this.additionalExpression.merge(delegate.getColumns());
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        }

        private List<Integer> createFilteredColumnIndex() {
            final List<Integer> result = new ArrayList<>();
            if (this.tableSeparator.includeColumns().isEmpty() && this.tableSeparator.excludeColumns().isEmpty()) {
                return result;
            }
            Arrays.stream(this.columns).forEach(col -> IntStream.range(0, this.allColumns.length)
                    .filter(i -> this.allColumns[i].getColumnName().equalsIgnoreCase(col.getColumnName()))
                    .findFirst()
                    .ifPresent(result::add));
            return result;
        }

        private HashMap<String, Integer> createColumnIndexes() {
            final HashMap<String, Integer> result = new HashMap<>();
            IntStream.range(0, this.columns.length)
                    .forEach(i -> result.put(this.columns[i].getColumnName().toUpperCase(), i));
            return result;
        }
    }
}
