package yo.dbunitcli.dataset;

import com.google.common.base.Objects;
import yo.dbunitcli.application.DataSourceType;

import java.io.File;

public class ComparableDataSetLoaderParam {
    private final File src;
    private final String encoding;
    private final DataSourceType source;
    private final ColumnSettings columnSettings;
    private final String headerSplitPattern;
    private final String dataSplitPattern;
    private final boolean mapIncludeMetaData;

    public ComparableDataSetLoaderParam(Builder builder) {
        this.src = builder.getSrc();
        this.encoding = builder.getEncoding();
        this.source = builder.getSource();
        this.columnSettings = builder.getColumnSettings();
        this.headerSplitPattern = builder.getHeaderSplitPattern();
        this.dataSplitPattern = builder.getDataSplitPattern();
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

    public boolean isMapIncludeMetaData() {
        return mapIncludeMetaData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparableDataSetLoaderParam that = (ComparableDataSetLoaderParam) o;
        return mapIncludeMetaData == that.mapIncludeMetaData &&
                Objects.equal(src, that.src) &&
                Objects.equal(encoding, that.encoding) &&
                source == that.source &&
                Objects.equal(columnSettings, that.columnSettings) &&
                Objects.equal(headerSplitPattern, that.headerSplitPattern) &&
                Objects.equal(dataSplitPattern, that.dataSplitPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(src, encoding, source, columnSettings, headerSplitPattern, dataSplitPattern, mapIncludeMetaData);
    }

    @Override
    public String toString() {
        return "ComparableDataSetLoaderParam{" +
                "src=" + src +
                ", encoding='" + encoding + '\'' +
                ", source=" + source +
                ", columnSettings=" + columnSettings +
                ", headerSplitPattern='" + headerSplitPattern + '\'' +
                ", dataSplitPattern='" + dataSplitPattern + '\'' +
                ", mapIncludeMetaData=" + mapIncludeMetaData +
                '}';
    }

    public static class Builder {
        private File src;
        private String encoding;
        private DataSourceType source;
        private ColumnSettings columnSettings;
        private String headerSplitPattern;
        private String dataSplitPattern;
        private boolean mapIncludeMetaData;

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
                return ColumnSettings.builder().build();
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

        public Builder setMapIncludeMetaData(boolean includeMetaData) {
            this.mapIncludeMetaData = includeMetaData;
            return this;
        }

        public ComparableDataSetLoaderParam build() {
            return new ComparableDataSetLoaderParam(this);
        }

    }
}
