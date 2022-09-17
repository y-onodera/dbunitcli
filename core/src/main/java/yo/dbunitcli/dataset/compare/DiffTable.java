package yo.dbunitcli.dataset.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.List;

public class DiffTable extends DefaultTable {

    public static final String C_MODIFY = "$MODIFY";
    public static final String C_ROW_AFTER_SORT = "$ROW_AFTER_SORT";
    public static final String C_DIFF_COLUMN_INDEXES = "$DIFF_COLUMN_INDEXES";
    public static final String C_ROW_ORIGINAL = "$ROW_ORIGINAL";
    public static final Column[] COLUMNS = {new Column(C_MODIFY, DataType.UNKNOWN)
            , new Column(C_ROW_AFTER_SORT, DataType.NUMERIC)
            , new Column(C_ROW_ORIGINAL, DataType.NUMERIC)
            , new Column(C_DIFF_COLUMN_INDEXES, DataType.UNKNOWN)};

    public static DiffTable from(ITableMetaData metaData, int columnLength) throws DataSetException {
        Column[] columns = toList(
                metaData.getColumns()
                , COLUMNS
        ).toArray(new Column[columnLength + 4]);
        Column[] primaryKeys = Lists.newArrayList(columns[1], columns[0]).toArray(new Column[2]);
        return new DiffTable(new DefaultTableMetaData(metaData.getTableName() + C_MODIFY, columns, primaryKeys));
    }

    private DiffTable(ITableMetaData metaData) {
        super(metaData);
    }

    public void addRow(CompareKeys compareKeys, Integer key, Object[] oldRow, Object[] newRow) throws DataSetException {
        this.addRow(toList(newRow,
                "NEW"
                , compareKeys.getRowNum()
                , compareKeys.getNewRowNum()
                , getIndexColumn(key))
                .toArray(new Object[oldRow.length + 4]));
        this.addRow(toList(oldRow
                , "OLD"
                , compareKeys.getRowNum()
                , compareKeys.getOldRowNum()
                , getIndexColumn(key))
                .toArray(new Object[oldRow.length + 4]));
    }

    public void addDiffColumn(CompareKeys targetKey, List<String> keys, Integer columnIndex) throws DataSetException {
        int replaceCount = 0;
        for (int rowNum = 0, total = this.getRowCount(); rowNum < total; rowNum++) {
            if (this.keyEquals(targetKey, keys, rowNum)) {
                this.setValue(rowNum, C_DIFF_COLUMN_INDEXES, getValue(rowNum, C_DIFF_COLUMN_INDEXES) + "," + getIndexColumn(columnIndex));
                if (++replaceCount > 2) {
                    break;
                }
            }
        }
    }

    protected boolean keyEquals(CompareKeys targetKey, List<String> keys, int rowNum) throws DataSetException {
        if (keys.size() > 0) {
            return targetKey.equals(this.getKey(rowNum, keys));
        }
        return targetKey.equals(this.getKey(rowNum, Lists.newArrayList(C_ROW_AFTER_SORT)));
    }

    protected CompareKeys getKey(int rowNum, List<String> keys) throws DataSetException {
        return new CompareKeys(this, rowNum, keys).oldRowNum(Integer.parseInt(this.getValue(rowNum, C_ROW_ORIGINAL).toString()));
    }

    protected String getIndexColumn(Integer key) throws DataSetException {
        return String.format("%s[%d]", this.getTableMetaData().getColumns()[key + 4].getColumnName(), key);
    }

    @SafeVarargs
    protected static <T> List<T> toList(T[] otherValues, T... newValues) {
        List<T> result = Lists.newArrayList(newValues);
        result.addAll(Lists.newArrayList(otherValues));
        return result;
    }
}
