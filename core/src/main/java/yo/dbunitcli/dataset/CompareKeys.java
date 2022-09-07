package yo.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.List;
import java.util.Objects;

public class CompareKeys {
    private final List<String> keys = Lists.newArrayList();
    private final int rowNum;
    private final int oldRowNum;
    private final int newRowNum;

    public CompareKeys(ITable table, int aRowNum, List<String> compareColumns) throws DataSetException {
        this.rowNum = aRowNum;
        this.oldRowNum = this.rowNum;
        this.newRowNum = this.rowNum;
        if (compareColumns.size() > 0) {
            for (String column : compareColumns) {
                keys.add(table.getValue(aRowNum, column).toString());
            }
        } else {
            keys.add(String.valueOf(aRowNum));
        }
    }

    public CompareKeys(int aRowNum, List<String> aKeys) {
        this(aRowNum, aRowNum, aRowNum, aKeys);
    }

    public CompareKeys(int rowNum, int originalOldRowNum, int originalNewRowNum, List<String> aKeys) {
        this.rowNum = rowNum;
        this.oldRowNum = originalOldRowNum;
        this.newRowNum = originalNewRowNum;
        this.keys.addAll(aKeys);
    }

    public String getKeysToString() {
        return keys.toString();
    }

    public int getRowNum() {
        return this.rowNum;
    }

    public int getOldRowNum() {
        return oldRowNum;
    }

    public int getNewRowNum() {
        return newRowNum;
    }

    public CompareKeys oldRowNum(int aOriginalRowNum) {
        return new CompareKeys(this.rowNum, aOriginalRowNum, this.newRowNum, this.keys);
    }

    public CompareKeys newRowNum(int aNewRowNum) {
        return new CompareKeys(this.rowNum, this.oldRowNum, aNewRowNum, this.keys);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompareKeys that = (CompareKeys) o;
        return Objects.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keys);
    }

    @Override
    public String toString() {
        return "CompareKeys{" +
                "keys=" + keys +
                ", rowNum=" + rowNum +
                ", oldRowNum=" + oldRowNum +
                ", newRowNum=" + newRowNum +
                '}';
    }
}
