package yo.dbunitcli.dataset;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.stream.IDataSetConsumer;

import java.util.Map;
import java.util.stream.Stream;

public interface ComparableDataSet extends IDataSet, IDataSetConsumer {

    @Override
    ComparableTable getTable(String tableName);

    Stream<Map<String, Object>> toMap();

    Stream<Map<String, Object>> toMap(boolean includeMetaData);

    boolean contains(String tableName);

    String getSrc();
}
