package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

public interface IDataSetWriter {

    void write(ITable aTable) throws DataSetException;

}
