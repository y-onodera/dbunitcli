package yo.dbunitcli.dataset;

public enum ResultType {
    csv, xls, xlsx, table, format;

    public DataSourceType toDataSourceType() {
        return DataSourceType.valueOf(this.toString());
    }
}
