package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class DatasetTableNamesRequestDto {

    // 基本
    private String srcType;
    private String src;

    // ファイルトラバーサル共通
    private String regTableInclude;
    private String regTableExclude;
    private boolean recursive;
    private String regInclude;
    private String regExclude;
    private String extension;

    // xlsx / xls 用
    private String xlsxSchema;

    // fixed 用
    private String fixedLength;

    // reg (regex) 用
    private String regHeaderSplit;
    private String regDataSplit;

    // csv / fixed / reg 共通
    private String encoding;
    private String delimiter;
    private boolean ignoreQuoted;

    // 汎用
    private String headerName;
    private String startRow;
    private boolean addFileInfo;

    // dataset setting ファイル名
    private String setting;

    // sql / table タイプ向け JDBC 情報
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPass;
    private String jdbcProperties;

    public String getSetting() {
        return this.setting;
    }

    public void setSetting(final String setting) {
        this.setting = setting;
    }

    public String getSrcType() {
        return this.srcType;
    }

    public void setSrcType(final String srcType) {
        this.srcType = srcType;
    }

    public String getSrc() {
        return this.src;
    }

    public void setSrc(final String src) {
        this.src = src;
    }

    public String getRegTableInclude() {
        return this.regTableInclude;
    }

    public void setRegTableInclude(final String regTableInclude) {
        this.regTableInclude = regTableInclude;
    }

    public String getRegTableExclude() {
        return this.regTableExclude;
    }

    public void setRegTableExclude(final String regTableExclude) {
        this.regTableExclude = regTableExclude;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    public void setRecursive(final boolean recursive) {
        this.recursive = recursive;
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

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }

    public String getXlsxSchema() {
        return this.xlsxSchema;
    }

    public void setXlsxSchema(final String xlsxSchema) {
        this.xlsxSchema = xlsxSchema;
    }

    public String getFixedLength() {
        return this.fixedLength;
    }

    public void setFixedLength(final String fixedLength) {
        this.fixedLength = fixedLength;
    }

    public String getRegHeaderSplit() {
        return this.regHeaderSplit;
    }

    public void setRegHeaderSplit(final String regHeaderSplit) {
        this.regHeaderSplit = regHeaderSplit;
    }

    public String getRegDataSplit() {
        return this.regDataSplit;
    }

    public void setRegDataSplit(final String regDataSplit) {
        this.regDataSplit = regDataSplit;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean isIgnoreQuoted() {
        return this.ignoreQuoted;
    }

    public void setIgnoreQuoted(final boolean ignoreQuoted) {
        this.ignoreQuoted = ignoreQuoted;
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public void setHeaderName(final String headerName) {
        this.headerName = headerName;
    }

    public String getStartRow() {
        return this.startRow;
    }

    public void setStartRow(final String startRow) {
        this.startRow = startRow;
    }

    public boolean isAddFileInfo() {
        return this.addFileInfo;
    }

    public void setAddFileInfo(final boolean addFileInfo) {
        this.addFileInfo = addFileInfo;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUser() {
        return this.jdbcUser;
    }

    public void setJdbcUser(final String jdbcUser) {
        this.jdbcUser = jdbcUser;
    }

    public String getJdbcPass() {
        return this.jdbcPass;
    }

    public void setJdbcPass(final String jdbcPass) {
        this.jdbcPass = jdbcPass;
    }

    public String getJdbcProperties() {
        return this.jdbcProperties;
    }

    public void setJdbcProperties(final String jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }
}
