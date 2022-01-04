package yo.dbunitcli.dataset;

import java.util.Objects;

public enum DataSourceType {
    TABLE("table") {
        @Override
        public boolean fromDatabase() {
            return true;
        }
    },
    SQL("sql") {
        @Override
        public boolean fromDatabase() {
            return true;
        }
    },
    FILE("file"),
    DIR("dir"),
    CSV("csv"),
    CSVQ("csvq"),
    REGSP("reg"),
    FIXED("fixed"),
    XLS("xls"),
    XLSX("xlsx");

    private final String type;

    DataSourceType(String name) {
        this.type = name;
    }

    public static DataSourceType fromString(String name) {
        for (DataSourceType type : DataSourceType.values()) {
            if (type.isEqual(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown type:" + name);
    }

    public boolean isEqual(String type) {
        return Objects.equals(this.type, type);
    }

    public boolean fromDatabase() {
        return false;
    }

    public String getType() {
        return type;
    }
}
