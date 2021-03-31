package yo.dbunitcli.dataset;

import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;

public class DataSetWriterParam {
    private final DatabaseConnectionLoader databaseConnectionLoader;
    private final String resultType;
    private final String operation;
    private final File resultDir;
    private final String outputEncoding;
    private final String excelTable;
    private final boolean exportEmptyTable;

    public DataSetWriterParam(Builder builder) {
        this.resultDir = builder.getResultDir();
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

    public String getResultType() {
        return resultType;
    }

    public String getOperation() {
        return operation;
    }

    public File getResultDir() {
        return resultDir;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public String getExcelTable() {
        return excelTable;
    }

    public boolean isExportEmptyTable() {
        return exportEmptyTable;
    }

    @Override
    public String toString() {
        return "DataSetWriterParam{" +
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
        private String resultType;
        private String operation;
        private File resultDir;
        private String outputEncoding;
        private String excelTable;
        private boolean exportEmptyTable;

        public DataSetWriterParam build() {
            return new DataSetWriterParam(this);
        }

        public DatabaseConnectionLoader getDatabaseConnectionLoader() {
            return this.databaseConnectionLoader;
        }

        public Builder setDatabaseConnectionLoader(DatabaseConnectionLoader databaseConnectionLoader) {
            this.databaseConnectionLoader = databaseConnectionLoader;
            return this;
        }

        public String getResultType() {
            return resultType;
        }

        public Builder setResultType(String resultType) {
            this.resultType = resultType;
            return this;
        }

        public String getOperation() {
            return operation;
        }

        public Builder setOperation(String operation) {
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
    }
}
