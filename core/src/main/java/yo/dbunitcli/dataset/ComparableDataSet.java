package yo.dbunitcli.dataset;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.IDataSetConsumer;

import java.util.List;
import java.util.Map;

public interface ComparableDataSet extends IDataSet, IDataSetConsumer {

    @Override
    ComparableTable getTable(String tableName);

    List<Map<String, Object>> toMap();

    List<Map<String, Object>> toMap(boolean includeMetaData);

    boolean contains(String tableName);

    String getSrc();
}
