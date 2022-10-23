package yo.dbunitcli.dataset.compare;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.util.function.Supplier;

public class CompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private AddSettingColumns comparisonKeys;
    private IDataSetConverter dataSetWriter;
    private Supplier<Compare.Manager> compareManagerFactory = DefaultCompareManager::new;

    public CompareBuilder oldDataSet(ComparableDataSet dataSet) {
        this.oldDataSet = dataSet;
        return this;
    }

    public CompareBuilder newDataSet(ComparableDataSet dataSet) {
        this.newDataSet = dataSet;
        return this;
    }

    public CompareBuilder comparisonKeys(AddSettingColumns comparisonKeys) {
        this.comparisonKeys = comparisonKeys;
        return this;
    }

    public CompareBuilder dataSetWriter(IDataSetConverter iDataSetWriter) {
        this.dataSetWriter = iDataSetWriter;
        return this;
    }

    public CompareBuilder setCompareManagerFactory(Supplier<Compare.Manager> compareManagerFactory) {
        this.compareManagerFactory = compareManagerFactory;
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

    public IDataSetConverter getDataSetWriter() {
        return this.dataSetWriter;
    }

    public Compare.Manager getManager() {
        return this.compareManagerFactory.get();
    }

    public File getResultDir() {
        return this.getDataSetWriter().getDir();
    }

    public Compare build() throws DataSetException {
        return new Compare(this);
    }

}
