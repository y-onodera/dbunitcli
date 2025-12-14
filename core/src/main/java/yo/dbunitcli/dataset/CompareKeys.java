package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record CompareKeys(List<String> keys, int rowNum, int oldRowNum, int newRowNum) {

    public CompareKeys(final ITable table, final int aRowNum, final List<String> compareColumns) {
        this(new ArrayList<>(), aRowNum, aRowNum, aRowNum);
        if (!compareColumns.isEmpty()) {
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
        this(new ArrayList<>(), rowNum, originalOldRowNum, originalNewRowNum);
        this.keys.addAll(aKeys);
    }

    public String getKeysToString() {
        return this.keys.toString();
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

}
