package yo.dbunitcli.dataset;

public enum DataSourceType {
    none,
    table,
    sql,
    file,
    dir,
    csv {
        @Override
        public String getExtension() {
            return this.name();
        }
    },
    csvq,
    reg,
    fixed,
    xls {
        @Override
        public String getExtension() {
            return this.name();
        }
    },
    xlsx {
        @Override
        public String getExtension() {
            return this.name();
        }
    };

    public String getExtension() {
        return null;
    }
}
