package yo.dbunitcli.dataset;

import java.util.List;
import java.util.Map;

public record TableAggregate(String name, String column, TableSeparator.RowFilter filter) {

    public List<Object> evaluate(final List<Map<String, Object>> rows) {
        return rows.stream()
                .filter(this.filter::test)
                .map(row -> row.get(this.column))
                .toList();
    }

}
