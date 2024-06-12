package yo.dbunitcli.dataset;

import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;

public record DataSetConverterParam(
        DatabaseConnectionLoader databaseConnectionLoader
        , ResultType resultType
        , DBConverter.Operation operation
        , File resultDir
        , String fileName
        , String outputEncoding
        , String excelTable
        , boolean exportEmptyTable) {

    public static Builder builder() {
        return new Builder();
    }

    public DataSetConverterParam(final Builder builder) {
        this(builder.getDatabaseConnectionLoader()
                , builder.getResultType()
                , builder.getOperation()
                , builder.getResultDir()
                , builder.getResultPath()
                , builder.getOutputEncoding()
                , builder.getExcelTable()
                , builder.isExportEmptyTable()
        );
    }

    public static class Builder {
        private DatabaseConnectionLoader databaseConnectionLoader;
        private ResultType resultType;
        private DBConverter.Operation operation;
        private File resultDir;
        private String outputEncoding;
        private String excelTable;
        private boolean exportEmptyTable;
        private String resultPath;

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

        public DBConverter.Operation getOperation() {
            return this.operation;
        }

        public Builder setOperation(final DBConverter.Operation operation) {
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

        public Builder setResultPath(final String resultPath) {
            this.resultPath = resultPath;
            return this;
        }

        public String getResultPath() {
            return this.resultPath;
        }
    }
}
