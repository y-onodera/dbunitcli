package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class SplitDto  {
    private String prefix;
    private String tableName;
    private String suffix;
    private List<String> breakKey;
    private List<String> filter;
    private int limit;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<String> getBreakKey() {
        return this.breakKey;
    }

    public void setBreakKey(final List<String> breakKey) {
        this.breakKey = breakKey;
    }

    public List<String> getFilter() {
        return this.filter;
    }

    public void setFilter(final List<String> filter) {
        this.filter = filter;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
