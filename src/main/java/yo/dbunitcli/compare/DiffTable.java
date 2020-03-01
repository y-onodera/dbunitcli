package yo.dbunitcli.compare;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.ColumnExpression;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.List;

public class DiffTable extends ComparableTable {

    protected static final String COLUMN_ROW_AFTER_SORT = "$ROW_AFTER_SORT";

    public static DiffTable from(ITableMetaData metaData, int columnLength) throws DataSetException {
        Column[] columns = toList(
                metaData.getColumns()
                , new Column("$MODIFY", DataType.UNKNOWN)
                , new Column(COLUMN_ROW_AFTER_SORT, DataType.NUMERIC)
                , new Column("$ROW_ORIGINAL", DataType.NUMERIC)
                , new Column("$DIFF_COLUMN_INDEXES", DataType.UNKNOWN)
        ).toArray(new Column[columnLength + 4]);
        Column[] primaryKeys = Lists.newArrayList(columns[1], columns[0]).toArray(new Column[2]);
        DefaultTableMetaData newMetaData = new DefaultTableMetaData(metaData.getTableName() + "$MODIFY", columns, primaryKeys);
        return new DiffTable(newMetaData);
    }

    private DiffTable(ITableMetaData metaData) throws DataSetException {
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
                this.replaceValue(rowNum, 3, getValue(rowNum, 3) + "," + getIndexColumn(columnIndex));
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
        return targetKey.equals(this.getKey(rowNum, Lists.newArrayList(COLUMN_ROW_AFTER_SORT)));
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
