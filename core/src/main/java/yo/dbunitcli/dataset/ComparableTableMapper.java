package yo.dbunitcli.dataset;

import org.dbunit.dataset.OrderedTableNameMap;

import java.util.List;
import java.util.Map;

public interface ComparableTableMapper {
    void startTable(IDataSetConverter converter, Map<String, Integer> alreadyWrite, List<ComparableTableJoin> joins);

    void addRow(Object[] values);

    void endTable(OrderedTableNameMap orderedTableNameMap);
}
