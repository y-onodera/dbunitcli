package yo.dbunitcli.dataset;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.util.Arrays;
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
    private final IDataSetWriter consumer;
    private final char delimiter;

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
        this.delimiter = builder.getDelimiter();
        this.databaseConnectionLoader = builder.getDatabaseConnectionLoader();
        this.consumer = builder.getConsumer();
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
        return Strings.nullToEmpty(Optional.ofNullable(this.extension)
                .orElse(this.source.getExtension()));
    }

    public char getDelimiter() {
        return delimiter;
    }

    public TemplateRender getStTemplateLoader() {
        return templateRender;
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return this.databaseConnectionLoader;
    }

    public IDataSetWriter getConsumer() {
        return this.consumer;
    }

    public File[] getSrcFiles() {
        if (this.getSrc().isDirectory()) {
            String end = "." + this.getExtension().toUpperCase();
            File[] result = this.getSrc().listFiles((file) -> file.isFile() && file.getName().toUpperCase().endsWith(end));
            Arrays.sort(result);
            return result;
        }
        return new File[]{this.getSrc()};
    }

    @Override
    public String toString() {
        return "ComparableDataSetParam{" +
                "src=" + src +
                ", encoding='" + encoding + '\'' +
                ", source=" + source +
                ", columnSettings=" + columnSettings +
                ", headerSplitPattern='" + headerSplitPattern + '\'' +
                ", dataSplitPattern='" + dataSplitPattern + '\'' +
                ", regInclude='" + regInclude + '\'' +
                ", regExclude='" + regExclude + '\'' +
                ", mapIncludeMetaData=" + mapIncludeMetaData +
                ", xlsxSchema=" + xlsxSchema +
                ", useJdbcMetaData=" + useJdbcMetaData +
                ", loadData=" + loadData +
                ", headerName='" + headerName + '\'' +
                ", fixedLength='" + fixedLength + '\'' +
                ", extension='" + extension + '\'' +
                ", templateRender=" + templateRender +
                ", databaseConnectionLoader=" + databaseConnectionLoader +
                ", delimiter=" + delimiter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComparableDataSetParam)) return false;
        ComparableDataSetParam that = (ComparableDataSetParam) o;
        return mapIncludeMetaData == that.mapIncludeMetaData && useJdbcMetaData == that.useJdbcMetaData && loadData == that.loadData && delimiter == that.delimiter && Objects.equal(src, that.src) && Objects.equal(encoding, that.encoding) && source == that.source && Objects.equal(columnSettings, that.columnSettings) && Objects.equal(headerSplitPattern, that.headerSplitPattern) && Objects.equal(dataSplitPattern, that.dataSplitPattern) && Objects.equal(regInclude, that.regInclude) && Objects.equal(regExclude, that.regExclude) && Objects.equal(xlsxSchema, that.xlsxSchema) && Objects.equal(headerName, that.headerName) && Objects.equal(fixedLength, that.fixedLength) && Objects.equal(extension, that.extension) && Objects.equal(templateRender, that.templateRender) && Objects.equal(databaseConnectionLoader, that.databaseConnectionLoader);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(src, encoding, source, columnSettings, headerSplitPattern, dataSplitPattern, regInclude, regExclude, mapIncludeMetaData, xlsxSchema, useJdbcMetaData, loadData, headerName, fixedLength, extension, templateRender, databaseConnectionLoader, delimiter);
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
        private IDataSetWriter consumer;
        private char delimiter = ',';

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
            return this.extension;
        }

        public char getDelimiter() {
            return delimiter;
        }

        public DatabaseConnectionLoader getDatabaseConnectionLoader() {
            return this.databaseConnectionLoader;
        }

        public IDataSetWriter getConsumer() {
            return consumer;
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

        public Builder editColumnSettings(Consumer<ColumnSettingEditor> function) {
            return this.setColumnSettings(this.getColumnSettings().apply(function));
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

        public Builder setDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder setConsumer(IDataSetWriter writer) {
            this.consumer = writer;
            return this;
        }

        public ComparableDataSetParam build() {
            return new ComparableDataSetParam(this);
        }
    }
}
