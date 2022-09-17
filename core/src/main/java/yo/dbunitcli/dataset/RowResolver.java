package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.RowOutOfBoundsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RowResolver {

    private final AddSettingTableMetaData addSettingTableMetaData;

    private final List<Object[]> values;

    private Integer[] _indexes;

    private final Column[] orderColumns;

    private final List<Integer> filteredRowIndexes;

    public RowResolver(Column[] orderColumns, AddSettingTableMetaData addSettingTableMetaData) {
        this.addSettingTableMetaData = addSettingTableMetaData;
        this.values = new ArrayList<>();
        this.orderColumns = orderColumns;
        this.filteredRowIndexes = new ArrayList<>();
    }

    public List<Object[]> getRows() throws RowOutOfBoundsException {
        List<Object[]> result = new ArrayList<>();
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
        if (this.addSettingTableMetaData.hasRowFilter()) {
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
        if (this.addSettingTableMetaData.hasRowFilter() && this.addSettingTableMetaData.applySetting(row) != null) {
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
        if (this.addSettingTableMetaData.hasRowFilter()) {
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
            List<Object[]> rows = new ArrayList<>();
            for (int i = 0, j = this.values.size(); i < j; i++) {
                if (!this.addSettingTableMetaData.hasRowFilter() || this.filteredRowIndexes.contains(i)) {
                    rows.add(this.addSettingTableMetaData.applySetting(this.values.get(i)));
                }
            }
            Integer[] columnIndex = Arrays.stream(this.orderColumns).map(it -> {
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

    protected Map<String, Object> rowToMap(Object[] row) {
        return this.addSettingTableMetaData.rowToMap(row);
    }

    protected boolean isSorted() {
        return this.orderColumns.length > 0;
    }

}
