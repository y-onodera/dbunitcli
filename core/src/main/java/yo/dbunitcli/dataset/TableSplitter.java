package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.List;

public record TableSplitter(TableRenameStrategy renameFunction, List<String> breakKeys, int limit) {

    public static TableSplitter NONE = new TableSplitter(new TableRenameStrategy.ReplaceFunction.Builder().build(), new ArrayList<>(), 0);

    public TableSplitter(final String newName, final String prefix, final String suffix, final int limit) {
        this(new TableRenameStrategy.ReplaceFunction(newName, prefix, suffix, limit > 0), new ArrayList<>(), limit);
    }

    public TableSplitter(final String newName, final String prefix, final String suffix, final List<String> breakKeys, final int limit) {
        this(new TableRenameStrategy.ReplaceFunction(newName, prefix, suffix, limit > 0), breakKeys, limit);

    }

    public TableSplitter(final Builder builder) {
        this(builder.getRenameFunction(), builder.getBreakKeys(), builder.getLimit());
    }

    public AddSettingTableMetaData getMetaData(final AddSettingTableMetaData metaData, final int no) {
        return metaData.rename(this.renameFunction.renameFunction().apply(metaData.getTableName(), no));
    }

    public boolean isLimit(final int addRowCount) {
        return this.isSplit() && this.limit <= addRowCount;
    }

    public boolean isSplit() {
        return this.limit > 0;
    }

    public Builder builder() {
        return new Builder().setLimit(this.limit)
                .setRenameFunction(this.renameFunction);
    }

    public static class Builder {
        private TableRenameStrategy renameFunction = NONE.renameFunction;

        private List<String> breakKeys = new ArrayList<>();

        private int limit;

        public TableSplitter build() {
            return new TableSplitter(this);
        }

        public TableRenameStrategy getRenameFunction() {
            return this.renameFunction;
        }

        public Builder setRenameFunction(final TableRenameStrategy renameFunction) {
            this.renameFunction = renameFunction;
            return this;
        }

        public List<String> getBreakKeys() {
            return this.breakKeys;
        }

        public void setBreakKeys(final List<String> breakKeys) {
            this.breakKeys = breakKeys;
        }

        public int getLimit() {
            return this.limit;
        }

        public Builder setLimit(final int limit) {
            this.limit = limit;
            return this;
        }
    }
}
