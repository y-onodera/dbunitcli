package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.RowOutOfBoundsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RowResolver {

    private final AddSettingTableMetaData addSettingTableMetaData;

    private final List<Object[]> values;

    private Integer[] _indexes;

    private final Predicate<Map<String, Object>> rowFilter;

    private final Column[] orderColumns;

    private final List<Integer> filteredRowIndexes;

    public RowResolver(List<Object[]> values, Column[] orderColumns, Predicate<Map<String, Object>> rowFilter, AddSettingTableMetaData addSettingTableMetaData) throws RowOutOfBoundsException {
        this.addSettingTableMetaData = addSettingTableMetaData;
        this.values = values;
        this.rowFilter = rowFilter;
        this.orderColumns = orderColumns;
        this.filteredRowIndexes = this.filteredRows();
    }

    public List<Object[]> getRows() throws RowOutOfBoundsException {
        List<Object[]> result = Lists.newArrayList();
        for (int i = 0, j = this.getRowCount(); i < j; i++) {
            result.add(getRow(i));
        }
        return result;
    }

    public Map<String, Object> getRowToMap(int rowNum) throws RowOutOfBoundsException {
        return this.rowToMap(this.getRow(rowNum));
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

    public void add(ComparableTable table) throws DataSetException {
        for (int rowNum = 0, total = table.getRowCount(); rowNum < total; rowNum++) {
            Column[] columns = this.addSettingTableMetaData.getColumns();
            Object[] row = new Object[columns.length];
            for (int i = 0, j = columns.length; i < j; i++) {
                row[i] = table.getValue(rowNum, columns[i].getColumnName());
            }
            this.add(row);
        }
    }

    public void add(Object[] row) {
        this.values.add(row);
        if (this.filteredRowIndexes != null && this.rowFilter.test(this.rowToMap(row))) {
            this.filteredRowIndexes.add(this.values.size() - 1);
        }
        this._indexes = null;
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
            List<Object[]> rows = Lists.newArrayList();
            for (int i = 0, j = values.size(); i < j; i++) {
                if (filteredRowIndexes == null || filteredRowIndexes.contains(i)) {
                    rows.add(addSettingTableMetaData.applySetting(values.get(i)));
                }
            }
            Integer[] columnIndex = Arrays.stream(orderColumns).map(it -> {
                        try {
                            return addSettingTableMetaData.getColumnIndex(it.getColumnName());
                        } catch (DataSetException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(Integer[]::new);
            Arrays.sort(indexes, (Integer i1, Integer i2) -> {
                try {
                    for (int i = 0, j = columnIndex.length; i < j; i++) {
                        Object value1 = rows.get(i1)[columnIndex[i]];
                        Object value2 = rows.get(i2)[columnIndex[i]];
                        if (value1 != null || value2 != null) {
                            if (value1 == null) {
                                return -1;
                            }
                            if (value2 == null) {
                                return 1;
                            }
                            int result = addSettingTableMetaData.getColumns()[i].getDataType().compare(value1, value2);
                            if (result != 0) {
                                return result;
                            }
                        }
                    }
                    return 0;
                } catch (DataSetException var10) {
                    throw new DatabaseUnitRuntimeException(var10);
                }
            });
            this._indexes = indexes;
        }
        return this._indexes[row];
    }

    protected List<Integer> filteredRows() {
        if (this.rowFilter == null) {
            return null;
        }
        int fullSize = this.values.size();
        List<Integer> filteredRowIndexes = new ArrayList<>();
        for (int row = 0; row < fullSize; ++row) {
            if (this.rowFilter.test(this.rowToMap(this.values.get(row)))) {
                filteredRowIndexes.add(row);
            }
        }
        return filteredRowIndexes;
    }

    protected Map<String, Object> rowToMap(Object[] row) {
        Map<String, Object> map = Maps.newHashMap();
        Column[] columns = this.addSettingTableMetaData.getColumns();
        for (int i = 0, j = row.length; i < j; i++) {
            map.put(columns[i].getColumnName(), row[i]);
        }
        return map;
    }

    protected boolean isSorted() {
        return this.orderColumns.length > 0;
    }

}
