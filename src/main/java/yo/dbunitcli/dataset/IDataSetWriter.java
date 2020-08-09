package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

public interface IDataSetWriter {

    void cleanupDirectory();

    default void open(String tableName) {
        // default no implementation
    }

    void write(ITable aTable) throws DataSetException;

    default void close() throws DataSetException {
        // default no implementation
    }
}
