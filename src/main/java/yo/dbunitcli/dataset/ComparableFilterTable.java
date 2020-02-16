package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dbunit.dataset.*;
import org.dbunit.dataset.filter.IColumnFilter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ComparableFilterTable extends ComparableTable {

    private final List<Integer> filterColumnIndex = Lists.newArrayList();

    public static ComparableTable createFrom(ITable table, Column[] orderColumns, IColumnFilter iColumnFilter) throws DataSetException {
        try {
            return new ComparableFilterTable(table.getTableMetaData(), getOriginRows(table), getComparator(table, orderColumns), iColumnFilter);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
    }

    public ComparableFilterTable(ITableMetaData tableMetaData, List<Object[]> values, Comparator<Object> comparator, IColumnFilter iColumnFilter) throws DataSetException {
        super(new ColumnFilterTable(new DefaultTable(tableMetaData), iColumnFilter).getTableMetaData(), values, comparator);
        Set<Column> noFilter = Sets.newHashSet(tableMetaData.getColumns());
        Set<Column> filtered = Sets.newHashSet(getDelegateMetaData().getColumns());
        for (Column column : Sets.difference(noFilter, filtered)) {
            this.filterColumnIndex.add(tableMetaData.getColumnIndex(column.getColumnName()));
        }
    }

    @Override
    public Object[] getRow(int rowNum) throws RowOutOfBoundsException {
        Object[] noFilter = super.getRow(rowNum);
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
