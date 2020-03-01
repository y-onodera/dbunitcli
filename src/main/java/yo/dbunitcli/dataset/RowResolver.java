package yo.dbunitcli.dataset;

import com.google.common.collect.Maps;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.RowOutOfBoundsException;

import java.util.*;
import java.util.function.Predicate;

public class RowResolver {

    private final AddSettingTableMetaData addSettingTableMetaData;

    private final List<Object[]> values;

    private Integer[] _indexes;

    private final Comparator<Object> rowComparator;

    private final List<Integer> filteredRowIndexes;

    public RowResolver(List<Object[]> values, Comparator<Object> rowComparator, Predicate<Map<String, Object>> rowFilter, AddSettingTableMetaData addSettingTableMetaData) throws RowOutOfBoundsException {
        this.addSettingTableMetaData = addSettingTableMetaData;
        this.values = values;
        this.rowComparator = rowComparator;
        this.filteredRowIndexes = this.filteredRows(rowFilter);
    }

    public Map<String, Object> getRowToMap(int rowNum) throws RowOutOfBoundsException {
        Object[] row = this.getRow(rowNum);
        Map<String, Object> map = Maps.newHashMap();
        Column[] columns = this.addSettingTableMetaData.getColumns();
        for (int i = 0, j = row.length; i < j; i++) {
            map.put(columns[i].getColumnName(), row[i]);
        }
        return map;
    }

    public Object[] getRow(int rowNum) throws RowOutOfBoundsException {
        if (rowNum < 0 || rowNum >= this.getRowCount()) {
            throw new RowOutOfBoundsException("rowNum " + rowNum + " is out of range;current row size is " + this.getRowCount());
        }
        return this.addSettingTableMetaData.applySetting(this.values.get(this.getRowIndex(rowNum)));
    }

    public int getRowCount() {
        if (this.filteredRowIndexes != null) {
            return this.filteredRowIndexes.size();
        }
        return this.values.size();
    }

    public void add(Object[] row) {
        this.values.add(row);
    }

    public void replaceValue(int row, int column, Object newValue) throws RowOutOfBoundsException {
        this.getRow(row)[column] = newValue;
    }

    public Object getValue(int i, String s) throws DataSetException {
        return this.getValue(i, this.addSettingTableMetaData.getColumnIndex(s));
    }

    public Object getValue(int i, int j) throws RowOutOfBoundsException {
        Object[] row = this.getRow(i);
        return row[j] == null ? "" : row[j];
    }

    public int getRowIndex(int noFilter) {
        int row = noFilter;
        if (this.filteredRowIndexes != null) {
            row = this.filteredRowIndexes.get(noFilter);
        }
        if (!this.isSorted()) {
            return row;
        }
        if (this._indexes == null) {
            Integer[] indexes = new Integer[this.values.size()];
            for (int i = 0; i < indexes.length; ++i) {
                indexes[i] = i;
            }
            Arrays.sort(indexes, this.rowComparator);
            this._indexes = indexes;
        }
        return this._indexes[row];
    }

    protected List<Integer> filteredRows(Predicate<Map<String, Object>> rowFilter) throws RowOutOfBoundsException {
        if (rowFilter == null) {
            return null;
        }
        int fullSize = this.values.size();
        List<Integer> filteredRowIndexes = new ArrayList<Integer>();
        for (int row = 0; row < fullSize; ++row) {
            if (rowFilter.test(this.getRowToMap(row))) {
                filteredRowIndexes.add(row);
            }
        }
        return filteredRowIndexes;
    }

    protected boolean isSorted() {
        return this.rowComparator != null;
    }

}
