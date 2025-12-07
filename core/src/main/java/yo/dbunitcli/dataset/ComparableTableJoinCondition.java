package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ComparableTableJoinCondition(String left, String right
        , ComparableTableJoin.Strategy strategy
        , List<TableSeparator> tableSeparators) {

    public static Builder builder() {
        return new Builder();
    }

    public ComparableTableJoinCondition(final Builder builder) {
        this(builder.getLeft(), builder.getRight(), builder.getStrategy(), builder.getTableSeparators());
    }

    public boolean hasRelation(final String tableName) {
        return Objects.equals(this.right(), tableName) || Objects.equals(this.left(), tableName);
    }

    public static class Builder {
        private final List<TableSeparator> tableSeparators = new ArrayList<>();
        private String left;
        private String right;
        private ComparableTableJoin.Strategy strategy = ComparableTableJoin.Strategy.NOT_JOIN;

        public ComparableTableJoinCondition build() {
            return new ComparableTableJoinCondition(this);
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

        public Builder addTableSeparators(final List<TableSeparator> tableSeparators) {
            this.tableSeparators.addAll(tableSeparators);
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

        public List<TableSeparator> getTableSeparators() {
            if (this.tableSeparators.isEmpty()) {
                return List.of(TableSeparator.NONE);
            }
            return this.tableSeparators;
        }
    }
}
