package example.y.onodera.dbunitcli.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;

import java.util.*;

public class CompareDiff {

    private final String targetName;

    private final Type diff;

    private final String oldDef;

    private final String newDef;

    private final int columnIndex;

    private final int rows;

    public CompareDiff(Builder builder) {
        this.targetName = builder.getTargetName();
        this.diff = builder.getDiff();
        this.oldDef = builder.getOldDef();
        this.newDef = builder.getNewDef();
        this.columnIndex = builder.getColumnIndex();
        this.rows = builder.getRows();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder getBuilder(Type columnsCount) {
        return builder().setDiff(columnsCount);
    }

    public static Collection<CompareDiff> deleteTable(Set<String> dropTables) {
        return tableDiff(dropTables, Type.TABLE_DELETE);
    }

    public static Collection<CompareDiff> addTable(Set<String> dropTables) {
        return tableDiff(dropTables, Type.TABLE_ADD);
    }

    public static Collection<CompareDiff> tableDiff(Set<String> diffTables, Type tableDiff) {
        List<CompareDiff> result = Lists.newArrayList();
        diffTables.stream()
                .forEach(name -> result.add(getBuilder(tableDiff)
                        .setTargetName(name)
                        .build()));
        return result;
    }

    public static Collection<CompareDiff> defineColumn(ITableMetaData oldMetaData, ITableMetaData newMetaData) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        final int newColumns = newMetaData.getColumns().length;
        final int oldColumns = oldMetaData.getColumns().length;
        if (oldColumns != newColumns) {
            result.add(getBuilder(Type.COLUMNS_COUNT)
                    .setTargetName(oldMetaData.getTableName())
                    .setOldDef(String.valueOf(oldColumns))
                    .setNewDef(String.valueOf(newColumns))
                    .build());
        }
        for (int i = 0; i < oldColumns; i++) {
            Column oldColumn = oldMetaData.getColumns()[i];
            if (i < newColumns) {
                Column newColumn = newMetaData.getColumns()[i];
                if (!Objects.equals(oldColumn.getColumnName(), newColumn.getColumnName())) {
                    result.add(getBuilder(Type.COLUMNS_MODIFY)
                            .setTargetName(oldMetaData.getTableName())
                            .setOldDef(oldColumn.getColumnName())
                            .setNewDef(newColumn.getColumnName())
                            .setColumnIndex(i)
                            .build());
                }
            } else {
                result.add(getBuilder(Type.COLUMNS_DELETE)
                        .setTargetName(oldMetaData.getTableName())
                        .setOldDef(oldColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
        if (oldColumns < newColumns) {
            for (int i = 0; oldColumns + i < newColumns; i++) {
                Column newColumn = newMetaData.getColumns()[oldColumns + i];
                result.add(getBuilder(Type.COLUMNS_ADD)
                        .setTargetName(oldMetaData.getTableName())
                        .setOldDef(newColumn.getColumnName())
                        .setColumnIndex(i)
                        .build());
            }
        }
        return result;
    }

    public static Collection<CompareDiff> defineRow(ComparableTable oldTable, ComparableTable newTable, List<String> keys) throws DataSetException {
        List<CompareDiff> result = Lists.newArrayList();
        result.addAll(rowCount(oldTable, newTable));
        final int oldRows = oldTable.getRowCount();
        final int columnLength = Math.min(newTable.getTableMetaData().getColumns().length, oldTable.getTableMetaData().getColumns().length);
        Map<CompareKeys, Object[]> newRowLists = newTable.getRows(columnLength, keys);
        Map<Integer, Integer> modifyValues = Maps.newHashMap();
        int deleteRows = 0;
        Set<CompareKeys> addRows = Sets.newHashSet(newRowLists.keySet());
        for (int rowNum = 0; rowNum < oldRows; rowNum++) {
            Object[] oldRow = oldTable.getRow(rowNum, columnLength);
            CompareKeys key = oldTable.getKey(rowNum, keys);
            if (newRowLists.containsKey(key)) {
                addRows.remove(key);
                Object[] newRow = newRowLists.get(key);
                for (int i = 0, j = oldRow.length; i < j; i++) {
                    if (!Objects.equals(oldRow[i], newRow[i])) {
                        final Integer current = modifyValues.get(Integer.valueOf(i));
                        if (current != null) {
                            modifyValues.put(Integer.valueOf(i), Integer.valueOf(current.intValue() + 1));
                        } else {
                            modifyValues.put(Integer.valueOf(i), Integer.valueOf(1));
                        }
                    }
                }
            } else {
                deleteRows++;
            }
        }
        if (modifyValues.size() > 0) {
            for (Map.Entry<Integer, Integer> entry : modifyValues.entrySet()) {
                result.add(getBuilder(Type.MODIFY_VALUE)
                        .setTargetName(oldTable.getTableMetaData().getTableName())
                        .setOldDef(oldTable.getTableMetaData().getColumns()[entry.getKey().intValue()].getColumnName())
                        .setNewDef(newTable.getTableMetaData().getColumns()[entry.getKey().intValue()].getColumnName())
                        .setColumnIndex(entry.getKey())
                        .setRows(entry.getValue())
                        .build());
            }
        }
        if (deleteRows > 0) {
            result.add(getBuilder(Type.KEY_DELETE)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(deleteRows)
                    .build());
        }
        if (addRows.size() > 0) {
            result.add(getBuilder(Type.KEY_ADD)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setRows(addRows.size())
                    .build());
        }
        return result;
    }

    private static List<CompareDiff> rowCount(ComparableTable oldTable, ComparableTable newTable) {
        List<CompareDiff> result = Lists.newArrayList();
        final int newRows = newTable.getRowCount();
        final int oldRows = oldTable.getRowCount();
        if (oldRows != newRows) {
            result.add(getBuilder(Type.ROWS_COUNT)
                    .setTargetName(oldTable.getTableMetaData().getTableName())
                    .setOldDef(String.valueOf(oldRows))
                    .setNewDef(String.valueOf(newRows))
                    .build());
        }
        return result;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getDiff() {
        return diff.name();
    }

    public String getOldDef() {
        return oldDef;
    }

    public String getNewDef() {
        return newDef;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public int getRows() {
        return rows;
    }

    public static enum Type {
        TABLE_COUNT, TABLE_DELETE, TABLE_ADD, COLUMNS_COUNT, COLUMNS_MODIFY, COLUMNS_DELETE, COLUMNS_ADD, ROWS_COUNT, KEY_DELETE, KEY_ADD, MODIFY_VALUE
    }

    public static class Builder {

        private String targetName;

        private Type diff;

        private String oldDef;

        private String newDef;

        private int columnIndex;

        private int rows;

        public CompareDiff build() {
            return new CompareDiff(this);
        }

        public String getTargetName() {
            return targetName;
        }

        public Builder setTargetName(String targetName) {
            this.targetName = targetName;
            return this;
        }

        public Type getDiff() {
            return diff;
        }

        public Builder setDiff(Type diff) {
            this.diff = diff;
            return this;
        }

        public String getOldDef() {
            return oldDef;
        }

        public Builder setOldDef(String oldDef) {
            this.oldDef = oldDef;
            return this;
        }

        public String getNewDef() {
            return newDef;
        }

        public Builder setNewDef(String newDef) {
            this.newDef = newDef;
            return this;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public Builder setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
            return this;
        }

        public int getRows() {
            return rows;
        }

        public Builder setRows(int rows) {
            this.rows = rows;
            return this;
        }
    }
}
