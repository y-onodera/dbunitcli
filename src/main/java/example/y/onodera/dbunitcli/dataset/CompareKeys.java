package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.util.List;
import java.util.Objects;

public class CompareKeys {
    private final List<String> keys = Lists.newArrayList();

    public CompareKeys(ITable table, int i, List<String> compareColumns) throws DataSetException {
        for (String column : compareColumns) {
            keys.add(table.getValue(i, column).toString());
        }
    }

    public CompareKeys(List<String> aKeys) {
        this.keys.addAll(aKeys);
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
                '}';
    }

}
