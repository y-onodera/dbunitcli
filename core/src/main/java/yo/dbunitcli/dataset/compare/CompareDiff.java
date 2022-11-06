package yo.dbunitcli.dataset.compare;

import java.util.function.UnaryOperator;

public class CompareDiff {

    private final String targetName;

    private final Diff diff;

    private final String oldDefine;

    private final String newDefine;

    private final int columnIndex;

    private final int rows;

    public CompareDiff(final Builder builder) {
        this.targetName = builder.getTargetName();
        this.diff = builder.getDiff();
        this.oldDefine = builder.getOldDefine();
        this.newDefine = builder.getNewDefine();
        this.columnIndex = builder.getColumnIndex();
        this.rows = builder.getRows();
    }

    public String getTargetName() {
        return this.targetName;
    }

    public String getDiff() {
        return this.diff.name();
    }

    public String getOldDefine() {
        return this.oldDefine;
    }

    public String getNewDefine() {
        return this.newDefine;
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    public int getRows() {
        return this.rows;
    }

    public CompareDiff edit(final UnaryOperator<Builder> function) {
        return function.apply(this.builder()).build();
    }

    private Builder builder() {
        return new Builder()
                .setTargetName(this.targetName)
                .setDiff(this.diff)
                .setNewDefine(this.newDefine)
                .setOldDefine(this.oldDefine)
                .setColumnIndex(this.columnIndex)
                .setRows(this.rows);
    }

    public static class Builder {

        private String targetName;

        private Diff diff;

        private String oldDefine;

        private String newDefine;

        private int columnIndex;

        private int rows;

        public CompareDiff build() {
            return new CompareDiff(this);
        }

        public String getTargetName() {
            return this.targetName;
        }

        public Builder setTargetName(final String targetName) {
            this.targetName = targetName;
            return this;
        }

        public Diff getDiff() {
            return this.diff;
        }

        public Builder setDiff(final Diff diff) {
            this.diff = diff;
            return this;
        }

        public String getOldDefine() {
            return this.oldDefine;
        }

        public Builder setOldDefine(final String oldDef) {
            this.oldDefine = oldDef;
            return this;
        }

        public String getNewDefine() {
            return this.newDefine;
        }

        public Builder setNewDefine(final String newDef) {
            this.newDefine = newDef;
            return this;
        }

        public int getColumnIndex() {
            return this.columnIndex;
        }

        public Builder setColumnIndex(final int columnIndex) {
            this.columnIndex = columnIndex;
            return this;
        }

        public int getRows() {
            return this.rows;
        }

        public Builder setRows(final int rows) {
            this.rows = rows;
            return this;
        }

    }

    public interface Diff {

        String name();

        default Builder of() {
            return new Builder().setDiff(this);
        }
    }

    public enum Type implements Diff {
        TABLE_COUNT, TABLE_DELETE, TABLE_ADD, COLUMNS_COUNT, COLUMNS_MODIFY, COLUMNS_DELETE, COLUMNS_ADD, ROWS_COUNT, KEY_DELETE, KEY_ADD, MODIFY_VALUE
    }
}
