package yo.dbunitcli.dataset;

import java.util.Objects;

public enum DataSourceType {
    TABLE("table"),
    SQL("sql"),
    FILE("file"),
    DIR("dir"),
    CSV("csv"),
    CSVQ("csvq"),
    REGSP("reg"),
    XLS("xls"),
    XLSX("xlsx");

    private final String name;

    DataSourceType(String name) {
        this.name = name;
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
        return Objects.equals(this.name, type);
    }

}
