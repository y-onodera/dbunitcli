package yo.dbunitcli.dataset;

import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;

public record DataSetConverterParam(DatabaseConnectionLoader databaseConnectionLoader, ResultType resultType,
                                    DbOperation operation, File resultDir, String fileName, String outputEncoding,
                                    String excelTable, String format, boolean exportEmptyTable, boolean exportHeader,
                                    String extension, String fixedColumnDefFile, String fixedLengthType) {


    public DataSetConverterParam(final Builder builder) {
        this(builder.getDatabaseConnectionLoader(), builder.getResultType(), builder.getOperation(),
             builder.getResultDir(), builder.getResultPath(), builder.getOutputEncoding(), builder.getExcelTable(),
             builder.getFormat(), builder.isExportEmptyTable(), builder.isSkipHeader(), builder.getExtension(),
             builder.getFixedColumnDefFile(), builder.getFixedLengthType());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String format() {
        return this.format;
    }

    public static class Builder {
        private DatabaseConnectionLoader databaseConnectionLoader;
        private ResultType resultType;
        private DbOperation operation;
        private File resultDir;
        private String outputEncoding;
        private String excelTable = "SHEET";
        private boolean exportEmptyTable;
        private boolean skipHeader;
        private String resultPath;
        private String format;
        private String extension;
        private String fixedColumnDefFile;
        private String fixedLengthType;

        public DataSetConverterParam build() {
            return new DataSetConverterParam(this);
        }

        public DatabaseConnectionLoader getDatabaseConnectionLoader() {
            return this.databaseConnectionLoader;
        }

        public Builder setDatabaseConnectionLoader(final DatabaseConnectionLoader databaseConnectionLoader) {
            this.databaseConnectionLoader = databaseConnectionLoader;
            return this;
        }

        public ResultType getResultType() {
            return this.resultType;
        }

        public Builder setResultType(final ResultType resultType) {
            this.resultType = resultType;
            return this;
        }

        public DbOperation getOperation() {
            return this.operation;
        }

        public Builder setOperation(final DbOperation operation) {
            this.operation = operation;
            return this;
        }

        public File getResultDir() {
            return this.resultDir;
        }

        public Builder setResultDir(final File resultDir) {
            this.resultDir = resultDir;
            return this;
        }

        public String getOutputEncoding() {
            return this.outputEncoding;
        }

        public Builder setOutputEncoding(final String outputEncoding) {
            this.outputEncoding = outputEncoding;
            return this;
        }

        public String getExcelTable() {
            return this.excelTable;
        }

        public Builder setExcelTable(final String excelTable) {
            this.excelTable = excelTable;
            return this;
        }

        public boolean isExportEmptyTable() {
            return this.exportEmptyTable;
        }

        public Builder setExportEmptyTable(final boolean exportEmptyTable) {
            this.exportEmptyTable = exportEmptyTable;
            return this;
        }

        public String getResultPath() {
            return this.resultPath;
        }

        public Builder setResultPath(final String resultPath) {
            this.resultPath = resultPath;
            return this;
        }

        public boolean isSkipHeader() {
            return this.skipHeader;
        }

        public Builder setSkipHeader(final boolean skipHeader) {
            this.skipHeader = skipHeader;
            return this;
        }

        public String getFormat() {
            return this.format;
        }

        public Builder setFormat(final String format) {
            this.format = format;
            return this;
        }

        public String getExtension() {
            return this.extension;
        }

        public Builder setExtension(final String extension) {
            this.extension = extension;
            return this;
        }

        public String getFixedColumnDefFile() {
            return this.fixedColumnDefFile;
        }

        public Builder setFixedColumnDefFile(final String fixedColumnDefFile) {
            this.fixedColumnDefFile = fixedColumnDefFile;
            return this;
        }

        public String getFixedLengthType() {
            return this.fixedLengthType;
        }

        public Builder setFixedLengthType(final String fixedLengthType) {
            this.fixedLengthType = fixedLengthType;
            return this;
        }
    }
}
