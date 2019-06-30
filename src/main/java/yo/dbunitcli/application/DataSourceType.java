package yo.dbunitcli.application;

import java.util.Objects;

public enum DataSourceType {
    CSV("csv", true),
    CSVQ("csvq", true),
    XLS("xls", false),
    XLSX("xlsx", false);

    private final String name;
    private final boolean needDir;

    DataSourceType(String name, boolean needDir) {
        this.name = name;
        this.needDir = needDir;
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

    public boolean isNeedDir() {
        return needDir;
    }
}
