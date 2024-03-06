package yo.dbunitcli.dataset;

public enum ResultType {
    csv, xls, xlsx, table;

    public DataSourceType toDataSourceType() {
        return DataSourceType.valueOf(this.toString());
    }
}
