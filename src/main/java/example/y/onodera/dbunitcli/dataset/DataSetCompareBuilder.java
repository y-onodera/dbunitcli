package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private Map<String, List<String>> comparisonKeys = Maps.newHashMap();

    public DataSetCompareBuilder oldDataSet(ComparableDataSet dataSet) {
        this.oldDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder newDataSet(ComparableDataSet dataSet) {
        this.newDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder comparisonKeys(Map<String, List<String>> comparisonKeys) {
        this.comparisonKeys = comparisonKeys;
        return this;
    }

    public ComparableDataSet getOldDataSet() {
        return oldDataSet;
    }

    public ComparableDataSet getNewDataSet() {
        return newDataSet;
    }

    public Map<String, List<String>> getComparisonKeys() {
        return comparisonKeys;
    }

    public Compare build() {
        return new DataSetCompare(this);
    }
}
