package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class AddSettingTableMetaData extends AbstractTableMetaData {
    private final String tableName;
    private final Column[] primaryKeys;
    private final Column[] columns;
    private final Column[] allColumns;
    private final ColumnExpression additionalExpression;
    private final List<Integer> filterColumnIndex;
    private final TableSeparator tableSeparator;
    private final Boolean distinct;
    private final AddSettingTableMetaData preset;

    public AddSettingTableMetaData(final ITableMetaData delegate
            , final Column[] primaryKeys
            , final IColumnFilter iColumnFilter
            , final TableSeparator tableSeparator
            , final Boolean distinct
            , final ColumnExpression additionalExpression) {
        this.tableName = tableSeparator.rename(delegate.getTableName());
        this.primaryKeys = primaryKeys;
        this.additionalExpression = additionalExpression;
        this.allColumns = this.getAllColumns(delegate);
        this.columns = this.getColumns(delegate, iColumnFilter);
        this.filterColumnIndex = this.getFilterColumnIndex(delegate);
        this.tableSeparator = tableSeparator;
        if (delegate instanceof AddSettingTableMetaData) {
            this.preset = (AddSettingTableMetaData) delegate;
        } else {
            this.preset = null;
        }
        this.distinct = distinct;
    }

    private AddSettingTableMetaData(final String tableName, final Column[] primaryKeys, final Column[] columns, final Column[] allColumns, final ColumnExpression additionalExpression, final List<Integer> filterColumnIndex, final TableSeparator tableSeparator, final AddSettingTableMetaData preset, final Boolean distinct) {
        this.tableName = tableName;
        this.primaryKeys = primaryKeys;
        this.columns = columns;
        this.allColumns = allColumns;
        this.additionalExpression = additionalExpression;
        this.filterColumnIndex = filterColumnIndex;
        this.tableSeparator = tableSeparator;
        this.preset = preset;
        this.distinct = distinct;
    }

    public AddSettingTableMetaData rename(final String newTableName) {
        return new AddSettingTableMetaData(newTableName
                , this.primaryKeys
                , this.columns
                , this.allColumns
                , this.additionalExpression
                , this.filterColumnIndex
                , this.tableSeparator
                , this.preset
                , this.distinct);
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

    public Boolean needDistinct() {
        return this.distinct || (this.preset != null && this.preset.needDistinct());
    }

    public Object[] applySetting(final Object[] values) {
        Object[] applySettings = values;
        if (this.preset != null) {
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

    public Rows distinct(final Collection<Object[]> values, final Collection<Integer> filteredRowIndexes) {
        Rows target = new Rows(values, filteredRowIndexes);
        if (this.preset != null && this.preset.needDistinct()) {
            target = this.preset.distinct(values, filteredRowIndexes);
        }
        if (!this.distinct) {
            return target;
        }
        return target.distinct(this::applySetting);
    }

    public boolean hasRowFilter() {
        return this.tableSeparator.hasRowFilter();
    }

    public TableSplitter getTableSplitter() {
        return this.tableSeparator.getSplitter();
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

    protected Map<String, Object> rowToMap(final Object[] row) {
        final Map<String, Object> map = new HashMap<>();
        IntStream.range(0, row.length).forEach(i -> map.put(this.columns[i].getColumnName(), row[i]));
        return map;
    }

    protected Object[] applyExpression(final Object[] objects) {
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

    protected Object[] filterColumn(final Object[] noFilter) {
        if (this.filterColumnIndex.size() == 0) {
            return noFilter;
        }
        return IntStream.range(0, noFilter.length)
                .filter(i -> !this.filterColumnIndex.contains(i))
                .mapToObj(i -> noFilter[i])
                .toArray(Object[]::new);
    }

    protected Column[] getAllColumns(final ITableMetaData delegate) {
        try {
            return this.additionalExpression.merge(delegate.getColumns());
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected Column[] getColumns(final ITableMetaData delegate, final IColumnFilter iColumnFilter) {
        try {
            if (iColumnFilter != null) {
                return this.additionalExpression.merge(new FilteredTableMetaData(delegate, iColumnFilter).getColumns());
            }
            return this.allColumns;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected List<Integer> getFilterColumnIndex(final ITableMetaData delegate) {
        try {
            final List<Integer> result = new ArrayList<>();
            final Set<Column> noFilter = new HashSet<>(Arrays.asList(delegate.getColumns()));
            final Set<Column> filtered = new HashSet<>(Arrays.asList(this.columns));
            noFilter.stream().filter(it -> !filtered.contains(it)).forEach(column -> {
                if (!this.additionalExpression.contains(column.getColumnName())) {
                    result.add(this.getColumnIndex(delegate, column));
                }
            });
            return result;
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected int getColumnIndex(final ITableMetaData delegate, final Column column) {
        try {
            return delegate.getColumnIndex(column.getColumnName());
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    public record Rows(List<Object[]> rows, List<Integer> filteredRowIndexes) {

        private static final BiPredicate<List<Object[]>, Object[]> DISTINCT_PREDICATE = (newRows, row) -> newRows.stream().noneMatch(it -> Arrays.equals(it, row));

        public Rows(final Collection<Object[]> rows, final Collection<Integer> filteredRowIndexes) {
            this(new ArrayList<>(rows), new ArrayList<>(filteredRowIndexes));
        }

        public int size() {
            return this.rows().size();
        }

        public boolean filtered() {
            return this.filteredRowIndexes().size() > 0;
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
                    if (this.filtered()) {
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

}
