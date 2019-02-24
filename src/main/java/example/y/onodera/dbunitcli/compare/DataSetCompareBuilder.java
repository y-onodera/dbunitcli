package example.y.onodera.dbunitcli.compare;

import com.google.common.collect.Maps;
import example.y.onodera.dbunitcli.dataset.ComparableDataSet;
import example.y.onodera.dbunitcli.dataset.IDataSetWriter;
import org.dbunit.dataset.DataSetException;

import java.util.List;
import java.util.Map;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private Map<String, List<String>> comparisonKeys = Maps.newHashMap();
    private IDataSetWriter dataSetWriter;

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

    public DataSetCompareBuilder dataSetWriter(IDataSetWriter iDataSetWriter) {
        this.dataSetWriter = iDataSetWriter;
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

    public IDataSetWriter getDataSetWriter() {
        return dataSetWriter;
    }

    public Compare build() throws DataSetException {
        return new DataSetCompare(this);
    }

}
