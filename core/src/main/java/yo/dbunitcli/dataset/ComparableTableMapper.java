package yo.dbunitcli.dataset;

import java.util.TreeMap;

public interface ComparableTableMapper {

    void startTable();

    void addRow(Object[] values);

    void endTable(TreeMap<String, ComparableTable> orderedTableNameMap);

}
