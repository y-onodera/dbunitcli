package yo.dbunitcli.dataset;

import org.dbunit.dataset.*;
import org.dbunit.dataset.stream.IDataSetConsumer;
import yo.dbunitcli.dataset.producer.ComparableFileDataSetProducer;

public class FileDataSet extends AbstractDataSet implements IDataSetConsumer {

    private final ColumnSettings compareSettings;

    private final ComparableDataSetParam param;

    private final ComparableDataSetProducer producer;

    private DefaultTable _activeTable;

    public FileDataSet(ComparableFileDataSetProducer producer) throws DataSetException {
        super(false);
        this.producer = producer;
        this.param = this.producer.getParam();
        this.compareSettings = this.param.getColumnSettings();
        this.initialize();
        producer.setConsumer(this);
        producer.produce();
    }

    @Override
    public void startDataSet() throws DataSetException {
        this._orderedTableNameMap = super.createTableNameMap();
    }

    @Override
    public void endDataSet() throws DataSetException {

    }

    @Override
    public void startTable(ITableMetaData iTableMetaData) throws DataSetException {
        this._activeTable = new DefaultTable(iTableMetaData);

    }

    @Override
    public void endTable() throws DataSetException {
        String tableName = this._activeTable.getTableMetaData().getTableName();
        if (this._orderedTableNameMap.containsTable(tableName)) {
            DefaultTable existingTable = (DefaultTable)this._orderedTableNameMap.get(tableName);
            existingTable.addTableRows(this._activeTable);
        } else {
            this._orderedTableNameMap.add(tableName, this._activeTable);
        }
        this._activeTable = null;
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        this._activeTable.addRow(values);
    }

    protected ITableIterator createIterator(boolean reversed) throws DataSetException {
        ITable[] tables = (ITable[])((ITable[])this._orderedTableNameMap.orderedValues().toArray(new ITable[0]));
        return new DefaultTableIterator(tables, reversed);
    }
}
