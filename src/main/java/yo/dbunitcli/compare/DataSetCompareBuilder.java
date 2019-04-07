package yo.dbunitcli.compare;

import com.google.common.collect.Maps;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;
import org.dbunit.dataset.DataSetException;

import java.util.List;
import java.util.Map;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private CompareSetting comparisonKeys;
    private IDataSetWriter dataSetWriter;

    public DataSetCompareBuilder oldDataSet(ComparableDataSet dataSet) {
        this.oldDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder newDataSet(ComparableDataSet dataSet) {
        this.newDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder comparisonKeys(CompareSetting comparisonKeys) {
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

    public CompareSetting getComparisonKeys() {
        return comparisonKeys;
    }

    public IDataSetWriter getDataSetWriter() {
        return dataSetWriter;
    }

    public Compare build() throws DataSetException {
        return new DataSetCompare(this);
    }

}
