package yo.dbunitcli.dataset;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparableTableDto extends HashMap<String, Object> {

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
    }

    public ComparableTableMappingTask.WithTargetTable getTask() {
        return this.task;
    }

    public String getTableName() {
        return this.get("tableName").toString();
    }

    public Column[] getColumns() {
        return (Column[]) this.get("columns");
    }

    public Column[] getPrimaryKeys() {
        return (Column[]) this.get("primaryKeys");
    }

    public Column[] getColumnsExcludeKey() {
        return (Column[]) this.get("columnsExcludeKey");
    }

    public Object getRows() {
        return this.get("rows");
    }

    public void setTableName(final String tableName) {
        this.put("tableName", tableName);
    }

    public void setColumns(final Column[] columns) {
        this.put("columns", columns);
    }

    public void setPrimaryKeys(final Column[] primaryKeys) {
        this.put("primaryKeys", primaryKeys);
    }

    public void setColumnsExcludeKey(final Column[] columnsExcludeKey) {
        this.put("columnsExcludeKey", columnsExcludeKey);
    }

    public void setRows(final List<Map<String, Object>> rows) {
        this.put("rows", rows);
    }

    public void setRows(final ComparableTableMappingTask.WithTargetTable task) {
        this.task = task;
        this.put("rows", task);
    }

}