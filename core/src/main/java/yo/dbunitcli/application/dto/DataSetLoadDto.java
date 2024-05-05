package yo.dbunitcli.application.dto;

import picocli.CommandLine;
import yo.dbunitcli.dataset.DataSourceType;

import java.util.stream.Stream;

public class DataSetLoadDto implements CompositeDto {
    @CommandLine.Option(names = "-src", description = "resource to load", required = true)
    private String src;
    @CommandLine.Option(names = "-srcType")
    private DataSourceType srcType;
    @CommandLine.Option(names = "-setting", description = "file comparison settings")
    private String setting;
    @CommandLine.Option(names = "-settingEncoding", description = "settings encoding")
    private String settingEncoding;
    @CommandLine.Option(names = "-loadData", description = "if false data row didn't load")
    private String loadData;
    @CommandLine.Option(names = "-includeMetaData", description = "whether param include tableName and columns or not ")
    private String includeMetaData;
    @CommandLine.Option(names = "-regInclude", description = "regex to include table")
    private String regInclude;
    @CommandLine.Option(names = "-regExclude", description = "regex to exclude table")
    private String regExclude;
    @CommandLine.Option(names = "-delimiter", description = "default is comma")
    private String delimiter;
    @CommandLine.Option(names = "-encoding", description = "csv file encoding")
    private String encoding = System.getProperty("file.encoding");
    @CommandLine.Option(names = "-xlsxSchema", description = "schema use read xlsx")
    private String xlsxSchemaSource;
    @CommandLine.Option(names = "-extension", description = "target extension")
    private String extension;
    @CommandLine.Option(names = "-fixedLength", description = "comma separate column Lengths")
    private String fixedLength;
    @CommandLine.Option(names = "-headerName", description = "comma separate header name. if set,all rows treat data rows")
    private String headerName;
    @CommandLine.Option(names = "-useJdbcMetaData", description = "default false. whether load metaData from jdbc or not")
    private String useJdbcMetaData = "false";
    @CommandLine.Option(names = "-recursive", description = "default true. whether traversal recursively")
    private String recursive = "true";
    @CommandLine.Option(names = "-regDataSplit", description = "regex to use split data row")
    private String regDataSplit;

    @CommandLine.Option(names = "-regHeaderSplit", description = "regex to use split header row")
    private String regHeaderSplit;
    private JdbcDto jdbc = new JdbcDto();

    private TemplateRenderDto templateRender = new TemplateRenderDto();

    @Override
    public Stream<Object> dto() {
        return Stream.of(this, this.jdbc, this.templateRender);
    }

    public String getSrc() {
        return this.src;
    }

    public void setSrc(final String src) {
        this.src = src;
    }

    public DataSourceType getSrcType() {
        return this.srcType;
    }

    public void setSrcType(final DataSourceType srcType) {
        this.srcType = srcType;
    }

    public String getSetting() {
        return this.setting;
    }

    public void setSetting(final String setting) {
        this.setting = setting;
    }

    public String getSettingEncoding() {
        return this.settingEncoding;
    }

    public void setSettingEncoding(final String settingEncoding) {
        this.settingEncoding = settingEncoding;
    }

    public String getLoadData() {
        return this.loadData;
    }

    public void setLoadData(final String loadData) {
        this.loadData = loadData;
    }

    public String getIncludeMetaData() {
        return this.includeMetaData;
    }

    public void setIncludeMetaData(final String includeMetaData) {
        this.includeMetaData = includeMetaData;
    }

    public String getRegInclude() {
        return this.regInclude;
    }

    public void setRegInclude(final String regInclude) {
        this.regInclude = regInclude;
    }

    public String getRegExclude() {
        return this.regExclude;
    }

    public void setRegExclude(final String regExclude) {
        this.regExclude = regExclude;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public String getXlsxSchemaSource() {
        return this.xlsxSchemaSource;
    }

    public void setXlsxSchemaSource(final String xlsxSchemaSource) {
        this.xlsxSchemaSource = xlsxSchemaSource;
    }

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }

    public String getFixedLength() {
        return this.fixedLength;
    }

    public void setFixedLength(final String fixedLength) {
        this.fixedLength = fixedLength;
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public void setHeaderName(final String headerName) {
        this.headerName = headerName;
    }

    public String getUseJdbcMetaData() {
        return this.useJdbcMetaData;
    }

    public void setUseJdbcMetaData(final String useJdbcMetaData) {
        this.useJdbcMetaData = useJdbcMetaData;
    }

    public String getRecursive() {
        return this.recursive;
    }

    public void setRecursive(final String recursive) {
        this.recursive = recursive;
    }

    public String getRegDataSplit() {
        return this.regDataSplit;
    }

    public void setRegDataSplit(final String regDataSplit) {
        this.regDataSplit = regDataSplit;
    }

    public String getRegHeaderSplit() {
        return this.regHeaderSplit;
    }

    public void setRegHeaderSplit(final String regHeaderSplit) {
        this.regHeaderSplit = regHeaderSplit;
    }

    public JdbcDto getJdbc() {
        return this.jdbc;
    }

    public void setJdbc(final JdbcDto jdbc) {
        this.jdbc = jdbc;
    }

    public TemplateRenderDto getTemplateRender() {
        return this.templateRender;
    }

    public void setTemplateRender(final TemplateRenderDto templateRender) {
        this.templateRender = templateRender;
    }
}
