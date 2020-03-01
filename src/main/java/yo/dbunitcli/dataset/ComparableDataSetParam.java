package yo.dbunitcli.dataset;

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
        return mapIncludeMetaData;
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

        public ComparableDataSetParam build() {
            return new ComparableDataSetParam(this);
        }

    }
}
