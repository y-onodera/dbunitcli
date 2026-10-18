package yo.dbunitcli.common;

import java.util.List;

public interface TargetFilter {

    default TargetFilter exclude(final List<String> names) {
        return new ExcludeFilter(this, names);
    }

    default TargetFilter or(final TargetFilter other) {
        return new OrFilter(this, other);
    }

    default TargetFilter and(final TargetFilter other) {
        return new AndFilter(this, other);
    }

    boolean test(String tableName);

    record ExcludeFilter(TargetFilter base, List<String> names) implements TargetFilter {
        @Override
        public boolean test(final String tableName) {
            return this.base().test(tableName) && !this.names().contains(tableName);
        }
    }

    record OrFilter(TargetFilter some, TargetFilter other) implements TargetFilter {
        @Override
        public boolean test(final String tableName) {
            return this.some.test(tableName) || this.other.test(tableName);
        }
    }

    record AndFilter(TargetFilter some, TargetFilter other) implements TargetFilter {
        @Override
        public boolean test(final String tableName) {
            return this.some.test(tableName) && this.other.test(tableName);
        }
    }
}
