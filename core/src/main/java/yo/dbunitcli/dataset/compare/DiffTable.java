package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import yo.dbunitcli.dataset.CompareKeys;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiffTable extends DefaultTable {

    public static final String C_MODIFY = "$MODIFY";
    public static final String C_ROW_AFTER_SORT = "$ROW_AFTER_SORT";
    public static final String C_DIFF_COLUMN_INDEXES = "$DIFF_COLUMN_INDEXES";
    public static final String C_ROW_ORIGINAL = "$ROW_ORIGINAL";
    public static final Column[] COLUMNS = {new Column(C_MODIFY, DataType.UNKNOWN)
            , new Column(C_ROW_AFTER_SORT, DataType.NUMERIC)
            , new Column(C_ROW_ORIGINAL, DataType.NUMERIC)
            , new Column(C_DIFF_COLUMN_INDEXES, DataType.UNKNOWN)};

    public static DiffTable from(final ITableMetaData metaData, final int columnLength) {
        try {
            final Column[] columns = toList(
                    metaData.getColumns()
                    , COLUMNS
            ).toArray(new Column[columnLength + 4]);
            final Column[] primaryKeys = new Column[]{columns[1], columns[0]};
            return new DiffTable(new DefaultTableMetaData(metaData.getTableName() + C_MODIFY, columns, primaryKeys));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    private DiffTable(final ITableMetaData metaData) {
        super(metaData);
    }

    public Collection<Integer> addRow(final CompareKeys compareKeys, final Integer columnIndex, final Object[] oldRow, final Object[] newRow) {
        try {
            this.addRow(toList(newRow,
                    "NEW"
                    , compareKeys.getRowNum()
                    , compareKeys.getNewRowNum()
                    , this.getIndexColumn(columnIndex))
                    .toArray(new Object[oldRow.length + 4]));
            this.addRow(toList(oldRow
                    , "OLD"
                    , compareKeys.getRowNum()
                    , compareKeys.getOldRowNum()
                    , this.getIndexColumn(columnIndex))
                    .toArray(new Object[oldRow.length + 4]));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
        final int rowCount = this.getRowCount();
        return Arrays.asList(rowCount - 1, rowCount - 2);
    }

    public Collection<Integer> rows(final CompareKeys targetKey, final List<String> keys) {
        final List<Integer> result = new ArrayList<>();
        IntStream.range(0, this.getRowCount()).forEach(rowNum -> {
            if (this.keyEquals(targetKey, keys, rowNum)) {
                result.add(rowNum);
            }
        });
        return result;
    }

    public void addDiffColumn(final Collection<Integer> rowNumbers, final Integer columnIndex) {
        rowNumbers.forEach(rowNum -> {
            try {
                this.setValue(rowNum, C_DIFF_COLUMN_INDEXES, this.getValue(rowNum, C_DIFF_COLUMN_INDEXES) + "," + this.getIndexColumn(columnIndex));
            } catch (final DataSetException e) {
                throw new AssertionError(e);
            }
        });
    }

    protected boolean keyEquals(final CompareKeys targetKey, final List<String> keys, final int rowNum) {
        if (keys.size() > 0) {
            return targetKey.equals(this.getKey(rowNum, keys));
        }
        return targetKey.equals(this.getKey(rowNum, Collections.singletonList(C_ROW_AFTER_SORT)));
    }

    protected CompareKeys getKey(final int rowNum, final List<String> keys) {
        try {
            return new CompareKeys(this, rowNum, keys).oldRowNum(Integer.parseInt(this.getValue(rowNum, C_ROW_ORIGINAL).toString()));
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    protected String getIndexColumn(final Integer key) {
        try {
            return String.format("%s[%d]", this.getTableMetaData().getColumns()[key + 4].getColumnName(), key);
        } catch (final DataSetException e) {
            throw new AssertionError(e);
        }
    }

    @SafeVarargs
    protected static <T> List<T> toList(final T[] otherValues, final T... newValues) {
        final List<T> result = Arrays.stream(newValues).collect(Collectors.toList());
        result.addAll(Arrays.asList(otherValues));
        return result;
    }
}
