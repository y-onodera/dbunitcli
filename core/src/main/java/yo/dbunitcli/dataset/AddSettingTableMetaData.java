package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.*;
import java.util.stream.IntStream;

public class AddSettingTableMetaData extends AbstractTableMetaData {
    private final String tableName;
    private final Column[] primaryKeys;
    private final Column[] columns;
    private final Column[] allColumns;
    private final ColumnExpression additionalExpression;
    private final List<Integer> filterColumnIndex;
    private final RowFilter rowFilter;
    private final AddSettingTableMetaData preset;

    public AddSettingTableMetaData(final ITableMetaData delegate
            , final Column[] primaryKeys
            , final IColumnFilter iColumnFilter
            , final RowFilter rowFilter
            , final ColumnExpression additionalExpression) {
        this.tableName = rowFilter.rename(delegate.getTableName());
        this.primaryKeys = primaryKeys;
        this.additionalExpression = additionalExpression;
        this.allColumns = this.getAllColumns(delegate);
        this.columns = this.getColumns(delegate, iColumnFilter);
        this.filterColumnIndex = this.getFilterColumnIndex(delegate);
        this.rowFilter = rowFilter;
        if (delegate instanceof AddSettingTableMetaData) {
            this.preset = (AddSettingTableMetaData) delegate;
        } else {
            this.preset = null;
        }
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

    public Object[] applySetting(final Object[] values) {
        Object[] applySettings = values;
        if (this.preset != null) {
            applySettings = this.preset.applySetting(applySettings);
            if (applySettings == null) {
                return null;
            }
        }
        final Object[] result = this.filterColumn(this.applyExpression(applySettings));
        if (this.hasRowFilter() && !this.rowFilter.test(this.rowToMap(result))) {
            return null;
        }
        return result;
    }

    public boolean hasRowFilter() {
        return this.rowFilter != null;
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
}
