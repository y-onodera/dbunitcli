package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private AddSettingColumns comparisonKeys;
    private IDataSetWriter dataSetWriter;

    public DataSetCompareBuilder oldDataSet(ComparableDataSet dataSet) {
        this.oldDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder newDataSet(ComparableDataSet dataSet) {
        this.newDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder comparisonKeys(AddSettingColumns comparisonKeys) {
        this.comparisonKeys = comparisonKeys;
        return this;
    }

    public DataSetCompareBuilder dataSetWriter(IDataSetWriter iDataSetWriter) {
        this.dataSetWriter = iDataSetWriter;
        return this;
    }

    public ComparableDataSet getOldDataSet() {
        return this.oldDataSet;
    }

    public ComparableDataSet getNewDataSet() {
        return this.newDataSet;
    }

    public AddSettingColumns getComparisonKeys() {
        return this.comparisonKeys;
    }

    public IDataSetWriter getDataSetWriter() {
        return this.dataSetWriter;
    }

    public TableDataSetCompare getTableDataSetCompare() {
        return new TableDataSetCompare();
    }

    public Compare build() throws DataSetException {
        return new DataSetCompare(this);
    }

}
