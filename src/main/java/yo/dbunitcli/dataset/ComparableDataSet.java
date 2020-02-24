package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.IDataSetConsumer;

import java.util.List;
import java.util.Map;

public interface ComparableDataSet extends IDataSet, IDataSetConsumer {

    String getSrc();

    List<Map<String, Object>> toMap() throws DataSetException;

    List<Map<String, Object>> toMap(boolean includeMetaData) throws DataSetException;

    @Override
    ComparableTable getTable(String tableName) throws DataSetException;

    boolean contains(String tableName);
}
