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

public class AddSettingTableMetaData extends AbstractTableMetaData {
    private final String tableName;
    private final Column[] primaryKeys;
    private final Column[] columns;
    private final ColumnExpression additionalExpression;
    private final List<Integer> filterColumnIndex = Lists.newArrayList();

    public AddSettingTableMetaData(String tableName, ITableMetaData delegate, Column[] primaryKeys, IColumnFilter iColumnFilter, ColumnExpression additionalExpression) throws DataSetException {
        this.tableName = tableName;
        this.primaryKeys = primaryKeys;
        if (iColumnFilter != null) {
            this.columns = additionalExpression.merge(new FilteredTableMetaData(delegate, iColumnFilter).getColumns());
        } else {
            this.columns = additionalExpression.merge(delegate.getColumns());
        }
        this.additionalExpression = additionalExpression;
        Set<Column> noFilter = Sets.newHashSet(delegate.getColumns());
        Set<Column> filtered = Sets.newHashSet(this.getColumns());
        for (Column column : Sets.difference(noFilter, filtered)) {
            if (!this.additionalExpression.contains(column.getColumnName())) {
                this.filterColumnIndex.add(delegate.getColumnIndex(column.getColumnName()));
            }
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

    public Object[] applySetting(Object[] objects) {
        return this.filterColumn(this.applyExpression(objects));
    }

    protected Object[] applyExpression(Object[] objects) {
        if (this.additionalExpression.size() == 0) {
            return objects;
        }
        Map<String, Object> param = Maps.newHashMap();
        for (int i = 0, j = objects.length; i < j; i++) {
            param.put(this.columns[i].getColumnName(), objects[i]);
        }
        Object[] result = Arrays.copyOf(objects, this.columns.length);
        for (int i = 0, j = result.length; i < j; i++) {
            String columnName = this.columns[i].getColumnName();
            if (this.additionalExpression.contains(columnName)) {
                result[i] = this.additionalExpression.evaluate(columnName, param);
                param.put(columnName, result[i]);
            }
        }
        return result;
    }

    protected Object[] filterColumn(Object[] noFilter) {
        if (this.filterColumnIndex.size() == 0) {
            return noFilter;
        }
        Object[] result = new Object[noFilter.length - this.filterColumnIndex.size()];
        int index = 0;
        for (int i = 0, j = noFilter.length; i < j; i++) {
            if (!this.filterColumnIndex.contains(i)) {
                result[index] = noFilter[i];
                index++;
            }
        }
        return result;
    }
}
