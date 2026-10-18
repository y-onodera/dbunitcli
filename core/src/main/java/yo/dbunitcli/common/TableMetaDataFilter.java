package yo.dbunitcli.common;

import org.dbunit.dataset.ITableMetaData;

import java.util.List;

public interface TableMetaDataFilter extends yo.dbunitcli.common.TargetFilter {

    default TableMetaDataFilter or(final TableMetaDataFilter other) {
        return new OrFilter(this, other);
    }

    default TableMetaDataFilter and(final TableMetaDataFilter other) {
        return new AndFilter(this, other);
    }

    @Override
    default TableMetaDataFilter exclude(final List<String> names) {
        return new ExcludeFilter(this, names);
    }

    default boolean test(final ITableMetaData metaData) {
        return this.test(metaData.getTableName());
    }

    record ExcludeFilter(TableMetaDataFilter base, List<String> names) implements TableMetaDataFilter {
        @Override
        public boolean test(final String tableName) {
            return this.base().test(tableName) && !this.names().contains(tableName);
        }
    }

    record OrFilter(TableMetaDataFilter some, TableMetaDataFilter other) implements TableMetaDataFilter {
        @Override
        public boolean test(final ITableMetaData metaData) {
            return this.some.test(metaData) || this.other.test(metaData);
        }

        @Override
        public boolean test(final String tableName) {
            return this.some.test(tableName) || this.other.test(tableName);
        }
    }

    record AndFilter(TableMetaDataFilter some, TableMetaDataFilter other) implements TableMetaDataFilter {
        @Override
        public boolean test(final ITableMetaData metaData) {
            return this.some.test(metaData) && this.other.test(metaData);
        }

        @Override
        public boolean test(final String tableName) {
            return this.some.test(tableName) && this.other.test(tableName);
        }
    }
}