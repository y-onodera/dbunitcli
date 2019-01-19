package example.y.onodera.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.IDataSetConsumer;

import java.util.List;
import java.util.Map;

public interface ComparableDataSet extends IDataSet, IDataSetConsumer {
    String getSrc();

    @Override
    ComparableTable getTable(String tableName) throws DataSetException;

}
