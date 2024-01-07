package yo.dbunitcli.dataset;

import java.util.Objects;

public record JoinCondition(String left, String right
        , ComparableTableJoin.Strategy strategy
        , TableSeparator tableSeparator) {

    public JoinCondition() {
        this("", "", ComparableTableJoin.Strategy.NOT_JOIN, TableSeparator.NONE);
    }

    public JoinCondition(final Builder builder) {
        this(builder.getLeft(), builder.getRight(), builder.getStrategy(), builder.getTableSeparator());
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasRelation(final String tableName) {
        return Objects.equals(this.right(), tableName) || Objects.equals(this.left(), tableName);
    }

    public static class Builder {
        private String left;
        private String right;
        private ComparableTableJoin.Strategy strategy = ComparableTableJoin.Strategy.NOT_JOIN;
        private TableSeparator tableSeparator = TableSeparator.NONE;

        public JoinCondition build() {
            return new JoinCondition(this);
        }

        public Builder setLeft(final String left) {
            this.left = left;
            return this;
        }

        public Builder setRight(final String right) {
            this.right = right;
            return this;
        }

        public Builder setStrategy(final ComparableTableJoin.Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder setTableSeparator(final TableSeparator tableSeparator) {
            this.tableSeparator = tableSeparator;
            return this;
        }

        public String getLeft() {
            return this.left;
        }

        public String getRight() {
            return this.right;
        }

        public ComparableTableJoin.Strategy getStrategy() {
            return this.strategy;
        }

        public TableSeparator getTableSeparator() {
            return this.tableSeparator;
        }
    }
}
