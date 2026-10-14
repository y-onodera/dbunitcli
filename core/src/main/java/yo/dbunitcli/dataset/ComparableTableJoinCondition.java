package yo.dbunitcli.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record ComparableTableJoinCondition(String left, String right
        , Pattern leftFilePath, Pattern rightFilePath
        , ComparableTableJoin.Strategy strategy
        , List<TableSeparator> tableSeparators) {

    public static Builder builder() {
        return new Builder();
    }

    public ComparableTableJoinCondition(final Builder builder) {
        this(builder.getLeft(), builder.getRight(), builder.getLeftFilePath(), builder.getRightFilePath(), builder.getStrategy(), builder.getTableSeparators());
    }

    public boolean hasRelation(final String tableName) {
        return Objects.equals(this.right(), tableName) || Objects.equals(this.left(), tableName);
    }

    public boolean hasRelation(final AddSettingTableMetaData metaData) {
        return this.isRight(metaData) || this.isLeft(metaData);
    }

    public boolean isLeft(final AddSettingTableMetaData metaData) {
        return Objects.equals(this.left(), metaData.getTableName()) && matchesFilePath(this.leftFilePath(), metaData);
    }

    public boolean isRight(final AddSettingTableMetaData metaData) {
        return Objects.equals(this.right(), metaData.getTableName()) && matchesFilePath(this.rightFilePath(), metaData);
    }

    private static boolean matchesFilePath(final Pattern filePath, final AddSettingTableMetaData metaData) {
        if (filePath == null) {
            return true;
        }
        return metaData.sourceFilePath() != null && filePath.matcher(metaData.sourceFilePath()).matches();
    }

    public static class Builder {
        private final List<TableSeparator> tableSeparators = new ArrayList<>();
        private String left;
        private String right;
        private Pattern leftFilePath;
        private Pattern rightFilePath;
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

        public Builder setLeftFilePath(final String leftFilePath) {
            this.leftFilePath = leftFilePath == null ? null : Pattern.compile(leftFilePath);
            return this;
        }

        public Builder setRightFilePath(final String rightFilePath) {
            this.rightFilePath = rightFilePath == null ? null : Pattern.compile(rightFilePath);
            return this;
        }

        public Pattern getLeftFilePath() {
            return this.leftFilePath;
        }

        public Pattern getRightFilePath() {
            return this.rightFilePath;
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
