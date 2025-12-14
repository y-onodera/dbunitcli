package yo.dbunitcli.dataset;

public interface ComparableTableMapper {

    void startTable();

    void addRow(Object[] values);

    void endTable();

}
