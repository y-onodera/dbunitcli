package yo.dbunitcli.dataset;

import com.google.common.base.Objects;
import yo.dbunitcli.application.DataSourceType;

import java.io.File;

public class ComparableDataSetLoaderParam {
    private final File src;
    private final String encoding;
    private final DataSourceType source;
    private final ColumnSetting excludeColumns;
    private final ColumnSetting orderColumns;
    private final String headerSplitPattern;
    private final String dataSplitPattern;

    public ComparableDataSetLoaderParam(Builder builder) {
        this.src = builder.getSrc();
        this.encoding = builder.getEncoding();
        this.source = builder.getSource();
        this.excludeColumns = builder.getExcludeColumns();
        this.orderColumns = builder.getOrderColumns();
        this.headerSplitPattern = builder.getHeaderSplitPattern();
        this.dataSplitPattern = builder.getDataSplitPattern();
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

    public ColumnSetting getExcludeColumns() {
        return this.excludeColumns;
    }

    public ColumnSetting getOrderColumns() {
        return this.orderColumns;
    }

    public String getHeaderSplitPattern() {
        return this.headerSplitPattern;
    }

    public String getDataSplitPattern() {
        return this.dataSplitPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparableDataSetLoaderParam that = (ComparableDataSetLoaderParam) o;
        return Objects.equal(this.src, that.src) &&
                Objects.equal(this.encoding, that.encoding) &&
                this.source == that.source &&
                Objects.equal(this.excludeColumns, that.excludeColumns) &&
                Objects.equal(this.headerSplitPattern, that.headerSplitPattern) &&
                Objects.equal(this.dataSplitPattern, that.dataSplitPattern);
    }

    @Override
    public String toString() {
        return "ComparableDataSetLoaderParam{" +
                "src=" + this.src +
                ", encoding='" + this.encoding + '\'' +
                ", source=" + this.source +
                ", excludeColumns=" + this.excludeColumns +
                ", headerSplitPattern='" + this.headerSplitPattern + '\'' +
                ", dataSplitPattern='" + this.dataSplitPattern + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private File src;
        private String encoding;
        private DataSourceType source;
        private ColumnSetting excludeColumns;
        private ColumnSetting orderColumns;
        private String headerSplitPattern;
        private String dataSplitPattern;

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

        public ColumnSetting getExcludeColumns() {
            if (this.excludeColumns == null) {
                return ColumnSetting.builder().build();
            }
            return this.excludeColumns;
        }

        public ColumnSetting getOrderColumns() {
            if (this.orderColumns == null) {
                return ColumnSetting.builder().build();
            }
            return this.orderColumns;
        }

        public String getHeaderSplitPattern() {
            return this.headerSplitPattern;
        }

        public String getDataSplitPattern() {
            return this.dataSplitPattern;
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

        public Builder setOrderColumns(ColumnSetting orderColumns) {
            this.orderColumns = orderColumns;
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
            return new ComparableDataSetLoaderParam(this);
        }
    }
}
