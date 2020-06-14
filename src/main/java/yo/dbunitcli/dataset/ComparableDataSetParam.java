package yo.dbunitcli.dataset;

import yo.dbunitcli.mapper.xlsx.XlsxSchema;

import java.io.File;

public class ComparableDataSetParam {
    private final File src;
    private final String encoding;
    private final DataSourceType source;
    private final ColumnSettings columnSettings;
    private final String headerSplitPattern;
    private final String dataSplitPattern;
    private final String regInclude;
    private final String regExclude;
    private final boolean mapIncludeMetaData;
    private final XlsxSchema xlsxSchema;
    private final boolean useJdbcMetaData;
    private final boolean loadData;

    public ComparableDataSetParam(Builder builder) {
        this.src = builder.getSrc();
        this.encoding = builder.getEncoding();
        this.source = builder.getSource();
        this.columnSettings = builder.getColumnSettings();
        this.headerSplitPattern = builder.getHeaderSplitPattern();
        this.dataSplitPattern = builder.getDataSplitPattern();
        this.regInclude = builder.getRegInclude();
        this.regExclude = builder.getRegExclude();
        this.mapIncludeMetaData = builder.getMapIncludeMetaData();
        this.xlsxSchema = builder.getXlsxSchema();
        this.useJdbcMetaData = builder.isUseJdbcMetaData();
        this.loadData = builder.isLoadData();
    }

    public static Builder builder() {
        return new Builder();
    }

    public File getSrc() {
        return this.src;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public DataSourceType getSource() {
        return this.source;
    }

    public ColumnSettings getColumnSettings() {
        return this.columnSettings;
    }

    public String getHeaderSplitPattern() {
        return this.headerSplitPattern;
    }

    public String getDataSplitPattern() {
        return this.dataSplitPattern;
    }

    public TableNameFilter getTableNameFilter() {
        return new TableNameFilter(this.regInclude, this.regExclude);
    }

    public boolean isMapIncludeMetaData() {
        return this.mapIncludeMetaData;
    }

    public XlsxSchema getXlsxSchema() {
        return this.xlsxSchema;
    }

    public boolean isUseJdbcMetaData() {
        return this.useJdbcMetaData;
    }

    public boolean isLoadData() {
        return loadData;
    }

    public static class Builder {
        private File src;
        private String encoding;
        private DataSourceType source;
        private ColumnSettings columnSettings;
        private String headerSplitPattern;
        private String dataSplitPattern;
        private boolean mapIncludeMetaData;
        private String regInclude;
        private String regExclude;
        private XlsxSchema xlsxSchema = XlsxSchema.DEFAULT;
        private boolean useJdbcMetaData;
        private boolean loadData = true;

        public Builder setSrc(File src) {
            this.src = src;
            return this;
        }

        public File getSrc() {
            return this.src;
        }

        public String getEncoding() {
            return this.encoding;
        }

        public DataSourceType getSource() {
            return this.source;
        }

        public ColumnSettings getColumnSettings() {
            if (this.columnSettings == null) {
                return ColumnSettings.NONE;
            }
            return this.columnSettings;
        }

        public String getHeaderSplitPattern() {
            return this.headerSplitPattern;
        }

        public String getDataSplitPattern() {
            return this.dataSplitPattern;
        }

        public boolean getMapIncludeMetaData() {
            return this.mapIncludeMetaData;
        }

        public String getRegInclude() {
            return this.regInclude;
        }

        public String getRegExclude() {
            return this.regExclude;
        }

        public XlsxSchema getXlsxSchema() {
            return this.xlsxSchema;
        }

        public boolean isUseJdbcMetaData() {
            return useJdbcMetaData;
        }

        public boolean isLoadData() {
            return this.loadData;
        }

        public Builder setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setSource(DataSourceType source) {
            this.source = source;
            return this;
        }

        public Builder setColumnSettings(ColumnSettings columnSettings) {
            this.columnSettings = columnSettings;
            return this;
        }

        public Builder setHeaderSplitPattern(String headerSplitPattern) {
            this.headerSplitPattern = headerSplitPattern;
            return this;
        }

        public Builder setDataSplitPattern(String dataSplitPattern) {
            this.dataSplitPattern = dataSplitPattern;
            return this;
        }

        public Builder setRegInclude(String regInclude) {
            this.regInclude = regInclude;
            return this;
        }

        public Builder setRegExclude(String regExclude) {
            this.regExclude = regExclude;
            return this;
        }

        public Builder setMapIncludeMetaData(boolean includeMetaData) {
            this.mapIncludeMetaData = includeMetaData;
            return this;
        }

        public Builder setXlsxSchema(XlsxSchema xlsxSchema) {
            this.xlsxSchema = xlsxSchema;
            return this;
        }

        public Builder setUseJdbcMetaData(boolean useJdbcMetaData) {
            this.useJdbcMetaData = useJdbcMetaData;
            return this;
        }

        public Builder setLoadData(boolean loadData) {
            this.loadData = loadData;
            return this;
        }

        public ComparableDataSetParam build() {
            return new ComparableDataSetParam(this);
        }

    }
}
