package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class AddSettingTableMetaData extends AbstractTableMetaData {
    private final String tableName;
    private final Column[] primaryKeys;
    private final Column[] columns;
    private final Column[] allColumns;
    private final ColumnExpression additionalExpression;
    private final List<Integer> filterColumnIndex = Lists.newArrayList();
    private final RowFilter rowFilter;

    public AddSettingTableMetaData(final ITableMetaData delegate
            , final Column[] primaryKeys
            , final IColumnFilter iColumnFilter
            , final RowFilter rowFilter
            , final ColumnExpression additionalExpression) throws DataSetException {
        this.primaryKeys = primaryKeys;
        this.allColumns = additionalExpression.merge(delegate.getColumns());
        if (iColumnFilter != null) {
            this.columns = additionalExpression.merge(new FilteredTableMetaData(delegate, iColumnFilter).getColumns());
        } else {
            this.columns = this.allColumns;
        }
        this.additionalExpression = additionalExpression;
        final Set<Column> noFilter = Sets.newHashSet(delegate.getColumns());
        final Set<Column> filtered = Sets.newHashSet(this.getColumns());
        Sets.difference(noFilter, filtered).forEach(column -> {
            if (!this.additionalExpression.contains(column.getColumnName())) {
                this.filterColumnIndex.add(this.getColumnIndex(delegate, column));
            }
        });
        this.rowFilter = rowFilter;
        this.tableName = this.rowFilter.rename(delegate.getTableName());
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

    public Object[] applySetting(final Object[] objects) {
        final Object[] result = this.filterColumn(this.applyExpression(objects));
        if (this.hasRowFilter() && !this.rowFilter.test(this.rowToMap(result))) {
            return null;
        }
        return result;
    }

    public boolean hasRowFilter() {
        return this.rowFilter != null;
    }

    protected Map<String, Object> rowToMap(final Object[] row) {
        final Map<String, Object> map = Maps.newHashMap();
        IntStream.range(0, row.length).forEach(i -> map.put(this.columns[i].getColumnName(), row[i]));
        return map;
    }

    protected Object[] applyExpression(final Object[] objects) {
        if (this.additionalExpression.size() == 0) {
            return objects;
        }
        final Map<String, Object> param = Maps.newHashMap();
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

    private int getColumnIndex(final ITableMetaData delegate, final Column column) {
        try {
            return delegate.getColumnIndex(column.getColumnName());
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

}
