package yo.dbunitcli.dataset;

import java.util.Objects;

public enum DataSourceType {
    TABLE("table",true,false,false),
    SQL("sql",true,true,false) ,
    FILE("file"),
    DIR("dir"),
    CSV("csv",false,false,true) ,
    CSVQ("csvq",false,true,false),
    REGSP("reg",false,false,true),
    FIXED("fixed",false,false,true),
    XLS("xls"),
    XLSX("xlsx");

    private final String type;

    private final boolean fromDatabase;

    private final boolean fromQuery;

    private final boolean noHeaderLoadable;

    DataSourceType(String type, boolean fromDatabase, boolean fromQuery, boolean loadableNoHeader) {
        this.type = type;
        this.fromDatabase = fromDatabase;
        this.noHeaderLoadable = loadableNoHeader;
        this.fromQuery = fromQuery;
    }

    DataSourceType(String name) {
        this(name, false, false, false);
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

    public boolean isUseDatabase() {
        return this.fromDatabase;
    }

    public boolean isNoHeaderLoadable() {
        return this.noHeaderLoadable;
    }

    public boolean isUseQuery() {
        return this.fromQuery;
    }

    public String getType() {
        return type;
    }
}
