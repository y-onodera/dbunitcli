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

    public static ComparableTable createFrom(ITable table, Column[] orderColumns, ColumnExpression additionalExpression, IColumnFilter iColumnFilter) throws DataSetException {
        try {
            return new ComparableFilterTable(table.getTableMetaData()
                    , additionalExpression.apply(new FilteredTableMetaData(table.getTableMetaData(), iColumnFilter))
                    , getOriginRows(table)
                    , getComparator(table, orderColumns));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new DataSetException(e);
        }
    }

    protected ComparableFilterTable(ITableMetaData originMetaData, AddExpressionTableMetaData tableMetaData, List<Object[]> values, Comparator<Object> comparator) throws DataSetException {
        super(tableMetaData, values, comparator);
        Set<Column> noFilter = Sets.newHashSet(originMetaData.getColumns());
        Set<Column> filtered = Sets.newHashSet(tableMetaData.getColumns());
        for (Column column : Sets.difference(noFilter, filtered)) {
            this.filterColumnIndex.add(originMetaData.getColumnIndex(column.getColumnName()));
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
