package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.List;

public record TableSplitter(TableRenameStrategy renameFunction, List<String> breakKeys, int limit) {

    public static TableSplitter NONE = new TableSplitter(new Builder());

    public TableSplitter(final Builder builder) {
        this(builder.getRenameFunction(), builder.getBreakKeys(), builder.getLimit());
    }

    public AddSettingTableMetaData getMetaData(final AddSettingTableMetaData metaData, final int no) {
        return metaData.rename(this.tableName(metaData.getTableName(), no));
    }

    /**
     * 分割前のテーブル名からno番目の分割後のテーブル名
     */
    public String tableName(final String baseTableName, final int no) {
        return this.renameFunction.renameFunction().apply(baseTableName, no);
    }

    public boolean isLimit(final int addRowCount) {
        return this.isSplit() && this.limit <= addRowCount;
    }

    public boolean isSplit() {
        return this.limit > 0;
    }

    /**
     * 分割の区切りを1行ずつ判定するカウンタ。行を保持して分割する場合も、行を流しながら分割する場合も同じ数え方にする
     */
    public Counter counter() {
        return new Counter();
    }

    public final class Counter {
        private final List<String> currentKeys = new ArrayList<>();
        private int keyCount;

        private Counter() {
        }

        /**
         * 行を今のテーブルに追加する前に呼ぶ。breakKeyがあれば1行目からのキーの変化を、無ければ今のテーブルの行数を数える
         *
         * @param keys     この行のbreakKeyの値（breakKeyが無ければ空）
         * @param rowCount 今のテーブルに書いた行数
         * @return trueならこの行から次のテーブルに切り替える
         */
        public boolean isBreak(final List<String> keys, final int rowCount) {
            if (!TableSplitter.this.breakKeys.isEmpty() && !this.currentKeys.equals(keys)) {
                if (!this.currentKeys.isEmpty()) {
                    this.keyCount++;
                }
                this.currentKeys.clear();
                this.currentKeys.addAll(keys);
            }
            if (TableSplitter.this.isLimit(TableSplitter.this.breakKeys.isEmpty() ? rowCount : this.keyCount)) {
                this.keyCount = 0;
                return true;
            }
            return false;
        }
    }

    public Builder builder() {
        return new Builder()
                .setLimit(this.limit)
                .setRenameFunction(this.renameFunction);
    }

    public static class Builder {
        private TableRenameStrategy renameFunction = new TableRenameStrategy.ReplaceFunction.Builder().build();
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

        public Builder setBreakKeys(final List<String> breakKeys) {
            this.breakKeys = breakKeys;
            return this;
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
