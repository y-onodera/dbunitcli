package yo.dbunitcli.dataset.compare;

import yo.dbunitcli.dataset.AddSettingColumns;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetConverter;

import java.io.File;
import java.util.function.Supplier;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private AddSettingColumns comparisonKeys;
    private IDataSetConverter dataSetConverter;
    private Supplier<DataSetCompare.Manager> compareManagerFactory = DefaultCompareManager::new;

    public DataSetCompareBuilder oldDataSet(final ComparableDataSet dataSet) {
        this.oldDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder newDataSet(final ComparableDataSet dataSet) {
        this.newDataSet = dataSet;
        return this;
    }

    public DataSetCompareBuilder comparisonKeys(final AddSettingColumns comparisonKeys) {
        this.comparisonKeys = comparisonKeys;
        return this;
    }

    public DataSetCompareBuilder dataSetConverter(final IDataSetConverter iDataSetConverter) {
        this.dataSetConverter = iDataSetConverter;
        return this;
    }

    public DataSetCompareBuilder setCompareManagerFactory(final Supplier<DataSetCompare.Manager> compareManagerFactory) {
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

    public IDataSetConverter getDataSetConverter() {
        return this.dataSetConverter;
    }

    public DataSetCompare.Manager getManager() {
        return this.compareManagerFactory.get();
    }

    public File getResultDir() {
        return this.getDataSetConverter().getDir();
    }

    public DataSetCompare build() {
        return new DataSetCompare(this);
    }

}
