package yo.dbunitcli.dataset;

import yo.dbunitcli.Strings;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;
import yo.dbunitcli.resource.poi.XlsxSchema;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public record ComparableDataSetParam(
        File src
        , String encoding
        , DataSourceType source
        , TableSeparators tableSeparators
        , String headerSplitPattern
        , String dataSplitPattern
        , NameFilter srcPathFilter
        , boolean mapIncludeMetaData
        , XlsxSchema xlsxSchema
        , NameFilter tableNameFilter
        , boolean useJdbcMetaData
        , int startRow
        , String headerName
        , boolean loadData
        , boolean addFileInfo
        , String fixedLength
        , char delimiter
        , boolean ignoreQuoted
        , String extension
        , boolean recursive
        , TemplateRender templateRender
        , DatabaseConnectionLoader databaseConnectionLoader
        , IDataSetConverter converter
) {

    public static Builder builder() {
        return new Builder();
    }

    public ComparableDataSetParam(final Builder builder) {
        this(builder.getSrc()
                , builder.getEncoding()
                , builder.getSource()
                , builder.getTableSeparators()
                , builder.getHeaderSplitPattern()
                , builder.getDataSplitPattern()
                , builder.getSrcPathFilter()
                , builder.isMapIncludeMetaData()
                , builder.getXlsxSchema()
                , builder.getTableNameFilter()
                , builder.isUseJdbcMetaData()
                , builder.getStartRow()
                , builder.getHeaderName()
                , builder.isLoadData()
                , builder.addFileInfo()
                , builder.getFixedLength()
                , builder.getDelimiter()
                , builder.isIgnoreQuoted()
                , builder.getExtension()
                , builder.isRecursive()
                , builder.getStTemplateLoader()
                , builder.getDatabaseConnectionLoader()
                , builder.getConverter()
        );
    }

    public Stream<File> getSrcFiles() {
        final Stream<File> result = this.src().isDirectory()
                ? this.getWalk().map(Path::toFile)
                : Stream.of(this.src());
        return result.filter(it -> it.isFile() && it.length() > 0
                && (Strings.isEmpty(this.extension) || it.getName().toUpperCase().endsWith("." + this.extension().toUpperCase())));
    }

    public Stream<Path> getWalk() {
        Stream<Path> walk = Stream.empty();
        try {
            walk = Files.walk(this.src.toPath(), this.recursive() ? Integer.MAX_VALUE : 1);
            return walk
                    .filter(path -> this.srcPathFilter().predicate(path.toString()));
        } catch (final Throwable e) {
            walk.close();
            throw new AssertionError(e);
        }
    }

    public String[] headerNames() {
        return Optional.ofNullable(this.headerName())
                .map(it -> it.split(","))
                .orElse(null);
    }

    public boolean addFileInfo() {
        return this.addFileInfo;
    }

    public Builder toBuilder() {
        return new Builder()
                .setSrc(this.src)
                .setEncoding(this.encoding)
                .setSource(this.source)
                .setTableSeparators(this.tableSeparators)
                .setHeaderSplitPattern(this.headerSplitPattern)
                .setDataSplitPattern(this.dataSplitPattern)
                .setRegInclude(this.srcPathFilter.include())
                .setRegExclude(this.srcPathFilter.exclude())
                .setMapIncludeMetaData(this.mapIncludeMetaData)
                .setXlsxSchema(this.xlsxSchema)
                .setRegTableInclude(this.tableNameFilter.include())
                .setRegTableExclude(this.tableNameFilter.exclude())
                .setUseJdbcMetaData(this.useJdbcMetaData)
                .setStartRow(this.startRow)
                .setHeaderName(this.headerName)
                .setLoadData(this.loadData)
                .setAddFileInfo(this.addFileInfo)
                .setFixedLength(this.fixedLength)
                .setDelimiter(this.delimiter)
                .setIgnoreQuoted(this.ignoreQuoted)
                .setExtension(this.extension)
                .setRecursive(this.recursive)
                .setSTTemplateLoader(this.templateRender)
                .setDatabaseConnectionLoader(this.databaseConnectionLoader)
                .setConverter(this.converter);
    }

    public static class Builder {
        private File src;
        private String encoding;
        private DataSourceType source;
        private TableSeparators tableSeparators;
        private String headerSplitPattern;
        private String dataSplitPattern;
        private boolean mapIncludeMetaData;
        private String regInclude;
        private String regExclude;
        private XlsxSchema xlsxSchema = XlsxSchema.DEFAULT;
        private boolean useJdbcMetaData;
        private int startRow = 1;
        private String headerName;
        private boolean loadData = true;
        private boolean addFileInfo = false;
        private String fixedLength;
        private String extension;
        private TemplateRender templateRender;
        private DatabaseConnectionLoader databaseConnectionLoader;
        private IDataSetConverter converter;
        private char delimiter = ',';
        private boolean recursive = false;
        private String regTableInclude;
        private String regTableExclude;
        private boolean ignoreQuoted = false;

        public ComparableDataSetParam build() {
            return new ComparableDataSetParam(this);
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

        public TableSeparators getTableSeparators() {
            if (this.tableSeparators == null) {
                return TableSeparators.NONE;
            }
            return this.tableSeparators;
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

        public NameFilter getSrcPathFilter() {
            return new NameFilter(this.regInclude, this.regExclude);
        }

        public XlsxSchema getXlsxSchema() {
            return this.xlsxSchema;
        }

        public NameFilter getTableNameFilter() {
            return new NameFilter(this.regTableInclude, this.regTableExclude);
        }

        public boolean isUseJdbcMetaData() {
            return this.useJdbcMetaData;
        }

        public boolean isLoadData() {
            return this.loadData;
        }

        public boolean addFileInfo() {
            return this.addFileInfo;
        }

        public int getStartRow() {
            return this.startRow;
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

        public String getRegTableInclude() {
            return this.regTableInclude;
        }

        public String getRegTableExclude() {
            return this.regTableExclude;
        }

        public boolean isIgnoreQuoted() {
            return this.ignoreQuoted;
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

        public Builder setSrc(final File src) {
            this.src = src;
            return this;
        }

        public Builder setEncoding(final String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setSource(final DataSourceType source) {
            this.source = source;
            return this;
        }

        public Builder editColumnSettings(final Consumer<TableSeparators.Builder> function) {
            this.tableSeparators = this.getTableSeparators().map(function);
            return this;
        }

        public Builder setTableSeparators(final TableSeparators tableSeparators) {
            this.tableSeparators = tableSeparators;
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

        public Builder setAddFileInfo(final boolean addFileInfo) {
            this.addFileInfo = addFileInfo;
            return this;
        }

        public Builder setStartRow(final int startRow) {
            this.startRow = startRow;
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

        public Builder setRegTableInclude(final String regTableInclude) {
            this.regTableInclude = regTableInclude;
            return this;
        }

        public Builder setRegTableExclude(final String regTableExclude) {
            this.regTableExclude = regTableExclude;
            return this;
        }

        public Builder setIgnoreQuoted(final boolean ignoreQuoted) {
            this.ignoreQuoted = ignoreQuoted;
            return this;
        }

    }
}
