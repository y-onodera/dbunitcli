package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompareKeys {
    private final List<String> keys = new ArrayList<>();
    private final int rowNum;
    private final int oldRowNum;
    private final int newRowNum;

    public CompareKeys(final ITable table, final int aRowNum, final List<String> compareColumns) {
        this.rowNum = aRowNum;
        this.oldRowNum = this.rowNum;
        this.newRowNum = this.rowNum;
        if (compareColumns.size() > 0) {
            compareColumns.forEach(column -> {
                try {
                    this.keys.add(table.getValue(aRowNum, column).toString());
                } catch (final DataSetException e) {
                    throw new AssertionError(e);
                }
            });
        } else {
            this.keys.add(String.valueOf(aRowNum));
        }
    }

    public CompareKeys(final int aRowNum, final List<String> aKeys) {
        this(aRowNum, aRowNum, aRowNum, aKeys);
    }

    public CompareKeys(final int rowNum, final int originalOldRowNum, final int originalNewRowNum, final List<String> aKeys) {
        this.rowNum = rowNum;
        this.oldRowNum = originalOldRowNum;
        this.newRowNum = originalNewRowNum;
        this.keys.addAll(aKeys);
    }

    public String getKeysToString() {
        return this.keys.toString();
    }

    public int getRowNum() {
        return this.rowNum;
    }

    public int getOldRowNum() {
        return this.oldRowNum;
    }

    public int getNewRowNum() {
        return this.newRowNum;
    }

    public CompareKeys oldRowNum(final int aOriginalRowNum) {
        return new CompareKeys(this.rowNum, aOriginalRowNum, this.newRowNum, this.keys);
    }

    public CompareKeys newRowNum(final int aNewRowNum) {
        return new CompareKeys(this.rowNum, this.oldRowNum, aNewRowNum, this.keys);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final CompareKeys that = (CompareKeys) o;
        return Objects.equals(this.keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.keys);
    }

    @Override
    public String toString() {
        return "CompareKeys{" +
                "keys=" + this.keys +
                ", rowNum=" + this.rowNum +
                ", oldRowNum=" + this.oldRowNum +
                ", newRowNum=" + this.newRowNum +
                '}';
    }
}
