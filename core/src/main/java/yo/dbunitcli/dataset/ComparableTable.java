package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.*;

import java.util.*;
import java.util.function.Predicate;


public class ComparableTable implements ITable {

    private final AddSettingTableMetaData addSettingTableMetaData;

    private final Column[] primaryKeys;

    private final Column[] columns;

    private final RowResolver rowResolver;

    protected ComparableTable(ITableMetaData metaData) throws DataSetException {
        this(ColumnExpression.builder().build().apply(metaData)
                , Lists.newArrayList()
                , new Column[]{}
                , null);
    }

    protected ComparableTable(AddSettingTableMetaData tableMetaData
            , List<Object[]> values
            , Column[] orderColumns
            , Predicate<Map<String, Object>> rowFilter) throws RowOutOfBoundsException {
        this.addSettingTableMetaData = tableMetaData;
        this.primaryKeys = this.addSettingTableMetaData.getPrimaryKeys();
        this.columns = this.addSettingTableMetaData.getColumns();
        this.rowResolver = new RowResolver(values, orderColumns, rowFilter, this.addSettingTableMetaData);
    }

    public List<Map<String, Object>> toMap() throws RowOutOfBoundsException {
        return this.toMap(false);
    }

    public List<Map<String, Object>> toMap(boolean includeMetaData) throws RowOutOfBoundsException {
        List<Map<String, Object>> result = Lists.newArrayList();
        String tableName = this.getTableMetaData().getTableName();
        Map<String, Object> withMetaDataMap = Maps.newHashMap();
        if (includeMetaData) {
            withMetaDataMap.put("tableName", tableName);
            withMetaDataMap.put("columns", this.columns);
            withMetaDataMap.put("primaryKeys", this.primaryKeys);
            result.add(withMetaDataMap);
        }
        List<Map<String, Object>> rowMap = Lists.newArrayList();
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            Map<String, Object> map = this.rowResolver.getRowToMap(rowNum);
            if (includeMetaData) {
                rowMap.add(map);
            } else {
                result.add(map);
            }
        }
        if (includeMetaData) {
            withMetaDataMap.put("row", rowMap);
        }
        return result;
    }

    @Override
    public ITableMetaData getTableMetaData() {
        return this.addSettingTableMetaData;
    }

    public Column[] getColumnsExcludeKey() throws DataSetException {
        List<Column> primaryKey = Arrays.asList(this.getTableMetaData().getPrimaryKeys());
        Column[] columns = this.getTableMetaData().getColumns();
        List<Column> list = new ArrayList<>();
        for (Column it : columns) {
            if (!primaryKey.contains(it)) {
                list.add(it);
            }
        }
        return list.toArray(new Column[0]);
    }

    public List<Object[]> getRows() throws RowOutOfBoundsException {
        return this.rowResolver.getRows();
    }

    public Map<CompareKeys, Map.Entry<Integer, Object[]>> getRows(List<String> keys) throws DataSetException {
        Map<CompareKeys, Map.Entry<Integer, Object[]>> result = Maps.newHashMap();
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            result.put(this.getKey(rowNum, keys), new AbstractMap.SimpleEntry<>(this.getOriginalRowIndex(rowNum), this.getRow(rowNum)));
        }
        if (result.size() < this.getRowCount()) {
            throw new AssertionError("comparison keys not unique:" + keys.toString());
        }
        return result;
    }

    @Override
    public int getRowCount() {
        return this.rowResolver.getRowCount();
    }

    public Object[] get(CompareKeys targetKey, List<String> keys, int columnLength) throws DataSetException {
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            if (targetKey.equals(this.getKey(rowNum, keys))) {
                return getRow(rowNum, columnLength);
            }
        }
        throw new AssertionError("keys not found:" + targetKey.toString());
    }

    public CompareKeys getKey(int rowNum, List<String> keys) throws DataSetException {
        return new CompareKeys(this, rowNum, keys).oldRowNum(this.getOriginalRowIndex(rowNum));
    }

    public Object[] getRow(int rowNum, int columnLength) throws RowOutOfBoundsException {
        Object[] row = getRow(rowNum);
        if (row.length < columnLength) {
            throw new AssertionError(columnLength + " is larger than columnLength:" + row.length);
        }
        Object[] resultRow = new Object[columnLength];
        System.arraycopy(row, 0, resultRow, 0, columnLength);
        return resultRow;
    }

    public Object[] getRow(int rowNum) throws RowOutOfBoundsException {
        return this.rowResolver.getRow(rowNum);
    }

    @Override
    public Object getValue(int i, String s) throws DataSetException {
        return this.rowResolver.getValue(i, s);
    }

    public Object getValue(int i, int j) throws RowOutOfBoundsException {
        return this.rowResolver.getValue(i, j);
    }

    public void addTableRows(ComparableTable table) throws DataSetException {
        this.rowResolver.add(table);
    }

    protected void replaceValue(int row, int column, Object newValue) throws RowOutOfBoundsException {
        this.rowResolver.replaceValue(row, column, newValue);
    }

    protected void addRow(Object[] row) {
        this.rowResolver.add(row);
    }

    protected int getOriginalRowIndex(int noFilter) {
        return this.rowResolver.getRowIndex(noFilter);
    }

}
