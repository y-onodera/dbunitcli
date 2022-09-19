package yo.dbunitcli.dataset;

import yo.dbunitcli.dataset.consumer.DBConsumer;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;

public class DataSetConsumerParam {
    private final DatabaseConnectionLoader databaseConnectionLoader;
    private final DataSourceType resultType;
    private final DBConsumer.Operation operation;
    private final File resultDir;
    private final String fileName;
    private final String outputEncoding;
    private final String excelTable;
    private final boolean exportEmptyTable;

    public DataSetConsumerParam(Builder builder) {
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

    public DBConsumer.Operation getOperation() {
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
                "databaseConnectionLoader=" + databaseConnectionLoader +
                ", resultType='" + resultType + '\'' +
                ", operation='" + operation + '\'' +
                ", resultDir=" + resultDir +
                ", outputEncoding='" + outputEncoding + '\'' +
                ", excelTable='" + excelTable + '\'' +
                ", exportEmptyTable=" + exportEmptyTable +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DatabaseConnectionLoader databaseConnectionLoader;
        private DataSourceType resultType;
        private DBConsumer.Operation operation;
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

        public Builder setDatabaseConnectionLoader(DatabaseConnectionLoader databaseConnectionLoader) {
            this.databaseConnectionLoader = databaseConnectionLoader;
            return this;
        }

        public DataSourceType getResultType() {
            return resultType;
        }

        public Builder setResultType(DataSourceType resultType) {
            this.resultType = resultType;
            return this;
        }

        public DBConsumer.Operation getOperation() {
            return operation;
        }

        public Builder setOperation(DBConsumer.Operation operation) {
            this.operation = operation;
            return this;
        }

        public File getResultDir() {
            return resultDir;
        }

        public Builder setResultDir(File resultDir) {
            this.resultDir = resultDir;
            return this;
        }

        public String getOutputEncoding() {
            return outputEncoding;
        }

        public Builder setOutputEncoding(String outputEncoding) {
            this.outputEncoding = outputEncoding;
            return this;
        }

        public String getExcelTable() {
            return excelTable;
        }

        public Builder setExcelTable(String excelTable) {
            this.excelTable = excelTable;
            return this;
        }

        public boolean isExportEmptyTable() {
            return exportEmptyTable;
        }

        public Builder setExportEmptyTable(boolean exportEmptyTable) {
            this.exportEmptyTable = exportEmptyTable;
            return this;
        }

        public Builder setResultPath(String resultPath) {
            this.resultPath = resultPath;
            return this;
        }

        public String getResultPath() {
            return this.resultPath;
        }
    }
}
