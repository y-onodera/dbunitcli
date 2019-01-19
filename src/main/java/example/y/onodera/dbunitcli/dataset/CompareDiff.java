package example.y.onodera.dbunitcli.dataset;

import com.google.common.base.Predicates;
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
