package yo.dbunitcli.dataset;

public enum ResultType {
    csv, xls, xlsx, table, format, fixed;

    public DataSourceType toDataSourceType() {
        return DataSourceType.valueOf(this.toString());
    }
}
