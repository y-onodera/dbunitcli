package yo.dbunitcli.dataset;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface ComparableTableMapper {
    void startTable(IDataSetConverter converter, Map<String, Integer> alreadyWrite, List<ComparableTableJoin> joins);

    void addRow(Object[] values);

    void endTable(TreeMap<String, ComparableTable> orderedTableNameMap);
}
