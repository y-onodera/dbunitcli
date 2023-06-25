package yo.dbunitcli.dataset;

import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;

public record DataSetConsumerParam(
        DatabaseConnectionLoader databaseConnectionLoader
        , DataSourceType resultType
        , DBConverter.Operation operation
        , File resultDir
        , String fileName
        , String outputEncoding
        , String excelTable
        , boolean exportEmptyTable) {

    public DataSetConsumerParam(final Builder builder) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DatabaseConnectionLoader databaseConnectionLoader;
        private DataSourceType resultType;
        private DBConverter.Operation operation;
        private File resultDir;
        private String outputEncoding;
        private String excelTable;
        private boolean exportEmptyTable;
        private String resultPath;

        public DataSetConsumerParam build() {
            return new DataSetConsumerParam(this);
        }

        public DatabaseConnectionLoader getDatabaseConnectionLoader() {
            return this.databaseConnectionLoader;
        }

        public Builder setDatabaseConnectionLoader(final DatabaseConnectionLoader databaseConnectionLoader) {
            this.databaseConnectionLoader = databaseConnectionLoader;
            return this;
        }

        public DataSourceType getResultType() {
            return this.resultType;
        }

        public Builder setResultType(final DataSourceType resultType) {
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
