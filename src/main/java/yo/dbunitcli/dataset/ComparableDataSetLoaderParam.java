package yo.dbunitcli.dataset;

import yo.dbunitcli.application.DataSourceType;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;

public class ComparableDataSetLoaderParam {
    private final File src;
    private final String encoding;
    private final DataSourceType source;
    private final ColumnSetting excludeColumns;
    private final String headerSplitPattern;
    private final String dataSplitPattern;

    public ComparableDataSetLoaderParam(File src, String encoding, DataSourceType source, ColumnSetting excludeColumns, String headerSplitPattern, String dataSplitPattern) {
        this.src = src;
        this.encoding = encoding;
        this.source = source;
        this.excludeColumns = excludeColumns;
        this.headerSplitPattern = headerSplitPattern;
        this.dataSplitPattern = dataSplitPattern;
    }

    public File getSrc() {
        return src;
    }

    public String getEncoding() {
        return encoding;
    }

    public DataSourceType getSource() {
        return source;
    }

    public ColumnSetting getExcludeColumns() {
        return excludeColumns;
    }

    public String getHeaderSplitPattern() {
        return headerSplitPattern;
    }

    public String getDataSplitPattern() {
        return dataSplitPattern;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private File src;
        private String encoding;
        private DataSourceType source;
        private ColumnSetting excludeColumns;
        private String headerSplitPattern;
        private String dataSplitPattern;

        public Builder setSrc(File src) {
            this.src = src;
            return this;
        }

        public Builder setEncoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder setSource(DataSourceType source) {
            this.source = source;
            return this;
        }

        public Builder setExcludeColumns(ColumnSetting excludeColumns) {
            this.excludeColumns = excludeColumns;
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

        public ComparableDataSetLoaderParam build() {
            return new ComparableDataSetLoaderParam(src, encoding, source, excludeColumns, headerSplitPattern, dataSplitPattern);
        }
    }
}