package yo.dbunitcli.dataset;

import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.function.Function;

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
    private final String headerName;
    private final String fixedLength;
    private final String extension;
    private final TemplateRender templateRender;
    private final DatabaseConnectionLoader databaseConnectionLoader;

    public ComparableDataSetParam(Builder builder) {
        this.src = builder.getSrc();
        this.encoding = builder.getEncoding();
        this.source = builder.getSource();
        this.columnSettings = builder.getColumnSettings();
        this.headerSplitPattern = builder.getHeaderSplitPattern();
        this.dataSplitPattern = builder.getDataSplitPattern();
        this.regInclude = builder.getRegInclude();
        this.regExclude = builder.getRegExclude();
        this.mapIncludeMetaData = builder.isMapIncludeMetaData();
        this.xlsxSchema = builder.getXlsxSchema();
        this.useJdbcMetaData = builder.isUseJdbcMetaData();
        this.loadData = builder.isLoadData();
        this.headerName = builder.getHeaderName();
        this.fixedLength = builder.getFixedLength();
        this.templateRender = builder.getStTemplateLoader();
        this.extension = builder.getExtension();
        this.databaseConnectionLoader = builder.getDatabaseConnectionLoader();
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
        return this.loadData;
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public String getFixedLength() {
        return this.fixedLength;
    }

    public String getExtension() {
        return extension;
    }

    public TemplateRender getStTemplateLoader() {
        return templateRender;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return this.databaseConnectionLoader;
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
        private String headerName;
        private String fixedLength;
        private String extension;
        private TemplateRender templateRender;
        private DatabaseConnectionLoader databaseConnectionLoader;

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

        public boolean isMapIncludeMetaData() {
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

        public String getHeaderName() {
            return this.headerName;
        }

        public String getFixedLength() {
            return this.fixedLength;
        }

        public TemplateRender getStTemplateLoader() {
            if (this.templateRender == null) {
                return new TemplateRender();
            }
            return templateRender;
        }

        public String getExtension() {
            return extension;
        }

        public DatabaseConnectionLoader getDatabaseConnectionLoader() {
            return this.databaseConnectionLoader;
        }

        public Builder ifMatch(boolean condition, Function<Builder, Builder> function) {
            if (!condition) {
                return this;
            }
            return function.apply(this);
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

        public Builder setHeaderName(String headerName) {
            this.headerName = headerName;
            return this;
        }

        public Builder setFixedLength(String fixedLength) {
            this.fixedLength = fixedLength;
            return this;
        }

        public Builder setSTTemplateLoader(TemplateRender templateRender) {
            this.templateRender = templateRender;
            return this;
        }

        public Builder setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        public Builder setDatabaseConnectionLoader(DatabaseConnectionLoader databaseConnectionLoader) {
            this.databaseConnectionLoader = databaseConnectionLoader;
            return this;
        }

        public ComparableDataSetParam build() {
            return new ComparableDataSetParam(this);
        }

    }
}
