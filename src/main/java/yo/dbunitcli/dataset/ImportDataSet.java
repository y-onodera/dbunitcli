package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;

public class ImportDataSet extends AbstractDataSet {

    private final ComparableDataSet target;

    public ImportDataSet(ComparableDataSet target) {
        this.target = target;
    }

    @Override
    public String[] getTableNames() throws DataSetException {
        return this.target.getTableNames();
    }

    @Override
    public ITableMetaData getTableMetaData(String s) throws DataSetException {
        return this.target.getTableMetaData(s);
    }

    @Override
    public ITable getTable(String s) throws DataSetException {
        return new ImportITable(this.target.getTable(s));
    }

    @Override
    protected ITableIterator createIterator(boolean reversed) throws DataSetException {
        return new Iterator(reversed ? this.target.reverseIterator() : this.target.iterator());
    }

    private class Iterator implements ITableIterator {
        private final ITableIterator _iterator;

        public Iterator(ITableIterator iterator) {
            this._iterator = iterator;
        }

        public boolean next() throws DataSetException {
            return this._iterator.next();
        }

        public ITableMetaData getTableMetaData() throws DataSetException {
            return this._iterator.getTableMetaData();
        }

        public ITable getTable() throws DataSetException {
            return new ImportITable(this._iterator.getTable());
        }
    }

    private class ImportITable implements ITable {
        private final ITable delegate;

        public ImportITable(ITable table) {
            this.delegate = table;
        }

        @Override
        public ITableMetaData getTableMetaData() {
            return this.delegate.getTableMetaData();
        }

        @Override
        public int getRowCount() {
            return this.delegate.getRowCount();
        }

        @Override
        public Object getValue(int i, String s) throws DataSetException {
            Object result = this.delegate.getValue(i, s);
            return "".equals(result) ? ITable.NO_VALUE : result;
        }
    }
}
