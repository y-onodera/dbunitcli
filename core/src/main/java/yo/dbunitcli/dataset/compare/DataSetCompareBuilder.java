package yo.dbunitcli.dataset.compare;

import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetConverter;
import yo.dbunitcli.dataset.TableSeparators;

import java.io.File;
import java.util.function.Supplier;

public class DataSetCompareBuilder {
    private ComparableDataSet oldDataSet;
    private ComparableDataSet newDataSet;
    private TableSeparators tableSeparators;
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

    public DataSetCompareBuilder tableSeparators(final TableSeparators tableSeparators) {
        this.tableSeparators = tableSeparators;
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

    public TableSeparators getTableSeparators() {
        return this.tableSeparators;
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
