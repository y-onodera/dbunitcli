package yo.dbunitcli.compare;

import org.dbunit.dataset.DataSetException;

public interface Compare {

    CompareResult result() throws DataSetException;

}