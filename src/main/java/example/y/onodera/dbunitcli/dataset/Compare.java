package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;

import java.util.List;
import java.util.Map;

public interface Compare {
    public CompareResult exec() throws DataSetException;
}