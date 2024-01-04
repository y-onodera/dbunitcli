package yo.dbunitcli.dataset;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public record JoinCondition(String outer, String inner
        , BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on
        , TableSeparator tableSeparator) {

    public static final BiFunction<Map<String, Object>, Map<String, Object>, Boolean> UNABLE_JOIN = (outer, inner) -> false;

    public JoinCondition() {
        this("", "", UNABLE_JOIN, TableSeparator.NONE);
    }

    public JoinCondition(final Builder builder) {
        this(builder.getOuter(), builder.getInner(), builder.getOn(), builder.getTableSeparator());
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasRelation(final String tableName) {
        return Objects.equals(this.inner(), tableName) || Objects.equals(this.outer(), tableName);
    }

    public static class Builder {
        private String outer;
        private String inner;
        private BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on = UNABLE_JOIN;
        private TableSeparator tableSeparator = TableSeparator.NONE;

        public JoinCondition build() {
            return new JoinCondition(this);
        }

        public Builder setOuter(final String outer) {
            this.outer = outer;
            return this;
        }

        public Builder setInner(final String inner) {
            this.inner = inner;
            return this;
        }

        public Builder setOn(final BiFunction<Map<String, Object>, Map<String, Object>, Boolean> on) {
            this.on = on;
            return this;
        }

        public Builder setTableSeparator(final TableSeparator tableSeparator) {
            this.tableSeparator = tableSeparator;
            return this;
        }

        public String getOuter() {
            return this.outer;
        }

        public String getInner() {
            return this.inner;
        }

        public BiFunction<Map<String, Object>, Map<String, Object>, Boolean> getOn() {
            return this.on;
        }

        public TableSeparator getTableSeparator() {
            return this.tableSeparator;
        }
    }
}
