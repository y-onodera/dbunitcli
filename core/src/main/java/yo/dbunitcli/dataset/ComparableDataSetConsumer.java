package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import yo.dbunitcli.common.TableMetaDataWithSource;

public interface ComparableDataSetConsumer {
    void startDataSet() throws DataSetException;

    void startTable(final TableMetaDataWithSource metaData);

    void row(Object[] values) throws DataSetException;

    void endTable() throws DataSetException;

    void endDataSet() throws DataSetException;

}
