package yo.dbunitcli.dataset;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public record ComparableDataSet(ComparableDataSetParam param, TreeMap<String, ComparableTable> tables, String src) {

    public Stream<Map<String, Object>> toMap() {
        return this.toMap(this.param.mapIncludeMetaData());
    }

    public Stream<Map<String, Object>> toMap(final boolean includeMetaData) {
        if (this.param.source() == DataSourceType.none) {
            return Stream.of(Map.of("rowNumber", 0));
        }
        return this.tables.keySet()
                .stream()
                .map(tableName -> this.getTable(tableName).toMap(includeMetaData))
                .flatMap(Collection::stream);
    }

    public boolean contains(final String tableName) {
        return this.tables.containsKey(tableName);
    }

    public ComparableTable getTable(final String tableName) {
        return this.tables.get(tableName);
    }

    public String[] getTableNames() {
        return this.tables.keySet().toArray(new String[0]);
    }

    public ComparableTable[] getTables() {
        return this.tables.values().toArray(new ComparableTable[0]);
    }
}
