package yo.dbunitcli.compare;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private ColumnSetting comparisonKeys;
    private IDataSetWriter dataSetWriter;

    public DataSetCompareBuilder oldDataSet(ComparableDataSet dataSet) {
        this.oldDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder newDataSet(ComparableDataSet dataSet) {
        this.newDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder comparisonKeys(ColumnSetting comparisonKeys) {
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

    public ColumnSetting getComparisonKeys() {
        return comparisonKeys;
    }

    public IDataSetWriter getDataSetWriter() {
        return dataSetWriter;
    }

    public Compare build() throws DataSetException {
        return new DataSetCompare(this);
    }

}
