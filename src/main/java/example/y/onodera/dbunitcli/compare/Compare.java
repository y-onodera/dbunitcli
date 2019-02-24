package example.y.onodera.dbunitcli.compare;

import org.dbunit.dataset.DataSetException;

public interface Compare {

    CompareResult result() throws DataSetException;

}