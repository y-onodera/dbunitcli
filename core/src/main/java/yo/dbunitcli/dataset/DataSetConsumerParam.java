package yo.dbunitcli.dataset;

import yo.dbunitcli.dataset.converter.DBConverter;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;

public class DataSetConsumerParam {
    private final DatabaseConnectionLoader databaseConnectionLoader;
    private final DataSourceType resultType;
    private final DBConverter.Operation operation;
    private final File resultDir;
    private final String fileName;
    private final String outputEncoding;
    private final String excelTable;
    private final boolean exportEmptyTable;

    public DataSetConsumerParam(final Builder builder) {
        this.resultDir = builder.getResultDir();
        this.fileName = builder.getResultPath();
        this.databaseConnectionLoader = builder.getDatabaseConnectionLoader();
        this.resultType = builder.getResultType();
        this.operation = builder.getOperation();
        this.outputEncoding = builder.getOutputEncoding();
        this.excelTable = builder.getExcelTable();
        this.exportEmptyTable = builder.isExportEmptyTable();
    }

    public DatabaseConnectionLoader getDatabaseConnectionLoader() {
        return this.databaseConnectionLoader;
    }

    public DataSourceType getResultType() {
        return this.resultType;
    }

    public DBConverter.Operation getOperation() {
        return this.operation;
    }

    public File getResultDir() {
        return this.resultDir;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getOutputEncoding() {
        return this.outputEncoding;
    }

    public String getExcelTable() {
        return this.excelTable;
    }

    public boolean isExportEmptyTable() {
        return this.exportEmptyTable;
    }

    @Override
    public String toString() {
        return "DataSetConsumerParam{" +
                "databaseConnectionLoader=" + this.databaseConnectionLoader +
                ", resultType='" + this.resultType + '\'' +
                ", operation='" + this.operation + '\'' +
                ", resultDir=" + this.resultDir +
                ", outputEncoding='" + this.outputEncoding + '\'' +
                ", excelTable='" + this.excelTable + '\'' +
                ", exportEmptyTable=" + this.exportEmptyTable +
                '}';
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
