package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import yo.dbunitcli.application.SplitOrNameFilter;

import java.util.*;
import java.util.function.Consumer;

public class ComparableTableDto extends HashMap<String, Object> {

    private final Map<String, Map<String, Object>> subTables = new LinkedHashMap<>();
    private ComparableTableMappingTask.WithTargetTable task;

    public ComparableTableDto(final ITableMetaData tableMetaData, final ComparableTableMappingTask task) {
        this(tableMetaData);
        this.setRows(task.withTargetTable(this.getTableName()));
    }

    public ComparableTableDto(final ITableMetaData tableMetaData, final List<Map<String, Object>> rows) {
        this(tableMetaData);
        this.setRows(rows);
    }

    public ComparableTableDto(final ITableMetaData tableMetaData) {
        this.setTableName(tableMetaData.getTableName());
        try {
            this.setColumns(tableMetaData.getColumns());
            this.setPrimaryKeys(tableMetaData.getPrimaryKeys());
            final List<Column> keys = Arrays.asList(tableMetaData.getPrimaryKeys());
            this.setColumnsExcludeKey(Arrays.stream(tableMetaData.getColumns())
                                            .filter(it -> !keys.contains(it))
                                            .toArray(Column[]::new));
        } catch (final DataSetException e) {
            throw new RuntimeException(e);
        }
        this.put("subTable", this.subTables);
    }

    public ComparableTableMappingTask.WithTargetTable getTask() {
        return this.task;
    }

    public String getTableName() {
        return this.get("tableName").toString();
    }

    public void setTableName(final String tableName) {
        this.put("tableName", tableName);
    }

    public Column[] getColumns() {
        return (Column[]) this.get("columns");
    }

    public void setColumns(final Column[] columns) {
        this.put("columns", columns);
    }

    public Column[] getPrimaryKeys() {
        return (Column[]) this.get("primaryKeys");
    }

    public void setPrimaryKeys(final Column[] primaryKeys) {
        this.put("primaryKeys", primaryKeys);
    }

    public Column[] getColumnsExcludeKey() {
        return (Column[]) this.get("columnsExcludeKey");
    }

    public void setColumnsExcludeKey(final Column[] columnsExcludeKey) {
        this.put("columnsExcludeKey", columnsExcludeKey);
    }

    public Object getRows() {
        return this.get("rows");
    }

    public void setRows(final List<Map<String, Object>> rows) {
        this.put("rows", rows);
    }

    public void setRows(final ComparableTableMappingTask.WithTargetTable task) {
        this.task = task;
        this.put("rows", task);
    }

    public Map<String, Map<String, Object>> getSubTables() {
        return this.subTables;
    }

    public void applyDataSetSeparators(TableSeparators dataSetSeparators) {
        String tableName = this.task.targetTableName();
        Consumer<TableSeparators.Builder> builder = origin ->
                origin.setFilter(origin.getFilter().and(new SplitOrNameFilter(tableName)))
                      .setNext(origin.getNext().then(dataSetSeparators));
        final TreeMap<String, ComparableTable> tables = this.task.runWith(builder, null);
        final ComparableTable target = tables.get(tableName);
        this.setRows(target == null ? new ArrayList<>() : target.toMap(false));
        tables.forEach((name, table) -> this.getSubTables().put(name, table.toMap(true).getFirst()));
    }

}