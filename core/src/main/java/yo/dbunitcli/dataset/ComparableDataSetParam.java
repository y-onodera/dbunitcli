package yo.dbunitcli.dataset;

import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
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
    private final IDataSetConverter converter;
    private final char delimiter;

    private final boolean recursive;

    public ComparableDataSetParam(final Builder builder) {
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
        this.delimiter = builder.getDelimiter();
        this.databaseConnectionLoader = builder.getDatabaseConnectionLoader();
        this.converter = builder.getConverter();
        this.recursive = builder.isRecursive();
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
        return Optional.ofNullable(this.extension)
                .orElse(Optional.ofNullable(this.source.getExtension()).orElse(""));
    }

    public char getDelimiter() {
        return this.delimiter;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    public TemplateRender getStTemplateLoader() {
        return this.templateRender;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return this.databaseConnectionLoader;
    }

    public IDataSetConverter getConverter() {
        return this.converter;
    }

    public File[] getSrcFiles() {
        if (this.getSrc().isDirectory()) {
            final String end = "." + this.getExtension().toUpperCase();
            final File[] result = this.getSrc().listFiles((file) -> file.isFile() && file.getName().toUpperCase().endsWith(end));
            Arrays.sort(result);
            return result;
        }
        return new File[]{this.getSrc()};
    }

    @Override
    public String toString() {
        return "ComparableDataSetParam{" +
                "src=" + this.src +
                ", encoding='" + this.encoding + '\'' +
                ", source=" + this.source +
                ", columnSettings=" + this.columnSettings +
                ", headerSplitPattern='" + this.headerSplitPattern + '\'' +
                ", dataSplitPattern='" + this.dataSplitPattern + '\'' +
                ", regInclude='" + this.regInclude + '\'' +
                ", regExclude='" + this.regExclude + '\'' +
                ", mapIncludeMetaData=" + this.mapIncludeMetaData +
                ", xlsxSchema=" + this.xlsxSchema +
                ", useJdbcMetaData=" + this.useJdbcMetaData +
                ", loadData=" + this.loadData +
                ", headerName='" + this.headerName + '\'' +
                ", fixedLength='" + this.fixedLength + '\'' +
                ", extension='" + this.extension + '\'' +
                ", templateRender=" + this.templateRender +
                ", databaseConnectionLoader=" + this.databaseConnectionLoader +
                ", delimiter=" + this.delimiter +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComparableDataSetParam)) {
            return false;
        }
        final ComparableDataSetParam that = (ComparableDataSetParam) o;
        return this.mapIncludeMetaData == that.mapIncludeMetaData && this.useJdbcMetaData == that.useJdbcMetaData && this.loadData == that.loadData && this.delimiter == that.delimiter && Objects.equals(this.src, that.src) && Objects.equals(this.encoding, that.encoding) && this.source == that.source && Objects.equals(this.columnSettings, that.columnSettings) && Objects.equals(this.headerSplitPattern, that.headerSplitPattern) && Objects.equals(this.dataSplitPattern, that.dataSplitPattern) && Objects.equals(this.regInclude, that.regInclude) && Objects.equals(this.regExclude, that.regExclude) && Objects.equals(this.xlsxSchema, that.xlsxSchema) && Objects.equals(this.headerName, that.headerName) && Objects.equals(this.fixedLength, that.fixedLength) && Objects.equals(this.extension, that.extension) && Objects.equals(this.templateRender, that.templateRender) && Objects.equals(this.databaseConnectionLoader, that.databaseConnectionLoader) && Objects.equals(this.converter, that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.src, this.encoding, this.source, this.columnSettings, this.headerSplitPattern, this.dataSplitPattern, this.regInclude, this.regExclude, this.mapIncludeMetaData, this.xlsxSchema, this.useJdbcMetaData, this.loadData, this.headerName, this.fixedLength, this.extension, this.templateRender, this.databaseConnectionLoader, this.converter, this.delimiter);
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
        private IDataSetConverter converter;
        private char delimiter = ',';
        private boolean recursive = true;

        public Builder setSrc(final File src) {
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

        public TemplateRender getStTemplateLoader() {
            if (this.templateRender == null) {
                return new TemplateRender();
            }
            return this.templateRender;
        }

        public String getExtension() {
            return this.extension;
        }

        public char getDelimiter() {
            return this.delimiter;
        }

        public boolean isRecursive() {
            return this.recursive;
        }

        public DatabaseConnectionLoader getDatabaseConnectionLoader() {
            return this.databaseConnectionLoader;
        }

        public IDataSetConverter getConverter() {
            return this.converter;
        }

        public Builder ifMatch(final boolean condition, final Function<Builder, Builder> function) {
            if (!condition) {
                return this;
            }
            return function.apply(this);
        }

        public Builder setEncoding(final String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setSource(final DataSourceType source) {
            this.source = source;
            return this;
        }

        public Builder editColumnSettings(final Consumer<ColumnSettingEditor> function) {
            return this.setColumnSettings(this.getColumnSettings().apply(function));
        }

        public Builder setColumnSettings(final ColumnSettings columnSettings) {
            this.columnSettings = columnSettings;
            return this;
        }

        public Builder setHeaderSplitPattern(final String headerSplitPattern) {
            this.headerSplitPattern = headerSplitPattern;
            return this;
        }

        public Builder setDataSplitPattern(final String dataSplitPattern) {
            this.dataSplitPattern = dataSplitPattern;
            return this;
        }

        public Builder setRegInclude(final String regInclude) {
            this.regInclude = regInclude;
            return this;
        }

        public Builder setRegExclude(final String regExclude) {
            this.regExclude = regExclude;
            return this;
        }

        public Builder setMapIncludeMetaData(final boolean includeMetaData) {
            this.mapIncludeMetaData = includeMetaData;
            return this;
        }

        public Builder setXlsxSchema(final XlsxSchema xlsxSchema) {
            this.xlsxSchema = xlsxSchema;
            return this;
        }

        public Builder setUseJdbcMetaData(final boolean useJdbcMetaData) {
            this.useJdbcMetaData = useJdbcMetaData;
            return this;
        }

        public Builder setLoadData(final boolean loadData) {
            this.loadData = loadData;
            return this;
        }

        public Builder setHeaderName(final String headerName) {
            this.headerName = headerName;
            return this;
        }

        public Builder setFixedLength(final String fixedLength) {
            this.fixedLength = fixedLength;
            return this;
        }

        public Builder setSTTemplateLoader(final TemplateRender templateRender) {
            this.templateRender = templateRender;
            return this;
        }

        public Builder setExtension(final String extension) {
            this.extension = extension;
            return this;
        }

        public Builder setDatabaseConnectionLoader(final DatabaseConnectionLoader databaseConnectionLoader) {
            this.databaseConnectionLoader = databaseConnectionLoader;
            return this;
        }

        public Builder setDelimiter(final char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder setConverter(final IDataSetConverter writer) {
            this.converter = writer;
            return this;
        }

        public Builder setRecursive(final boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public ComparableDataSetParam build() {
            return new ComparableDataSetParam(this);
        }

    }
}
