package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.HashSet;
import java.util.Set;

public record TableMetaDataCollector(Set<ITableMetaData> items) implements IDataSetConverter {
    public TableMetaDataCollector() {
        this(new HashSet<>());
    }

    @Override
    public boolean isExportEmptyTable() {
        return true;
    }

    @Override
    public void reStartTable(final AddSettingTableMetaData tableMetaData, final Integer writeRows) {

    }

    @Override
    public IDataSetConverter split() {
        return this;
    }

    @Override
    public void startTable(final ITableMetaData metaData) throws DataSetException {
        this.items.add(metaData);
    }

    @Override
    public void endTable() throws DataSetException {

    }

    @Override
    public void row(final Object[] values) throws DataSetException {

    }
}
