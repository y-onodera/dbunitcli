package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.sql.SQLException;

public interface IDataSetWriter {

    default void open(String tableName) {
        // default no implementation
    }

    void write(ITable aTable) throws DataSetException;

    default void close() throws DataSetException {
        // default no implementation
    }

}
