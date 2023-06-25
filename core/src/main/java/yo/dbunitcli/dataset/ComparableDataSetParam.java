package yo.dbunitcli.dataset;

import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public record ComparableDataSetParam(
        File src,
        String encoding,
        DataSourceType source,
        ColumnSettings columnSettings,
        String headerSplitPattern,
        String dataSplitPattern,
        TableNameFilter tableNameFilter,
        boolean mapIncludeMetaData,
        XlsxSchema xlsxSchema,
        boolean useJdbcMetaData,
        boolean loadData,
        String headerName,
        String fixedLength,
        char delimiter,
        String extension,
        boolean recursive,
        TemplateRender templateRender,
        DatabaseConnectionLoader databaseConnectionLoader,
        IDataSetConverter converter
) {

    public ComparableDataSetParam(final Builder builder) {
        this(builder.getSrc(),
                builder.getEncoding(),
                builder.getSource(),
                builder.getColumnSettings(),
                builder.getHeaderSplitPattern(),
                builder.getDataSplitPattern(),
                builder.getTableNameFilter(),
                builder.isMapIncludeMetaData(),
                builder.getXlsxSchema(),
                builder.isUseJdbcMetaData(),
                builder.isLoadData(),
                builder.getHeaderName(),
                builder.getFixedLength(),
                builder.getDelimiter(),
                builder.getExtension(),
                builder.isRecursive(),
                builder.getStTemplateLoader(),
                builder.getDatabaseConnectionLoader(),
                builder.getConverter()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public File[] getSrcFiles() {
        if (this.src().isDirectory()) {
            final String end = "." + this.extension().toUpperCase();
            final File[] result = this.getWalk()
                    .map(Path::toFile)
                    .filter(it -> it.isFile() && it.getName().toUpperCase().endsWith(end))
                    .toArray(File[]::new);
            Arrays.sort(result);
            return result;
        }
        return new File[]{this.src()};
    }


    public Stream<Path> getWalk() {
        try {
            if (this.recursive) {
                return Files.walk(this.src.toPath());
            }
            return Files.walk(this.src.toPath(), 1);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }
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

        public TableNameFilter getTableNameFilter() {
            return new TableNameFilter(this.regInclude, this.regExclude);
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
            return Objects.requireNonNullElseGet(this.templateRender, TemplateRender::new);
        }

        public String getExtension() {
            return Optional.ofNullable(this.extension)
                    .orElse(this.source == null ? "" : this.source.getExtension());
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
