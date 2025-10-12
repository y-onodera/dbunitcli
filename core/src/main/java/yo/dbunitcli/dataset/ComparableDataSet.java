package yo.dbunitcli.dataset;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

import java.util.Map;
import java.util.stream.Stream;

public interface ComparableDataSet extends IDataSet {

    @Override
    ComparableTable getTable(String tableName);

    void startDataSet() throws DataSetException;

    void endDataSet() throws DataSetException;

    void startTable(TableMetaDataWithSource metaData) throws DataSetException;

    void endTable() throws DataSetException;

    void row(Object[] values) throws DataSetException;

    Stream<Map<String, Object>> toMap();

    Stream<Map<String, Object>> toMap(boolean includeMetaData);

    boolean contains(String tableName);

    String getSrc();
}
