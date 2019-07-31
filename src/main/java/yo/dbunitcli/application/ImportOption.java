package yo.dbunitcli.application;

import org.dbunit.dataset.DataSetException;
import org.dbunit.operation.CloseConnectionOperation;
import org.dbunit.operation.DatabaseOperation;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import yo.dbunitcli.dataset.ImportDataSet;

import java.io.File;

public class ImportOption extends CommandLineOption {

    @Option(name = "-src", usage = "import target", required = true)
    private File src;

    @Option(name = "-srcType", usage = "csv | csvq | xls | xlsx : default csv")
    private String srcType = "csv";

    @Option(name = "-op", usage = "import operation UPDATE | INSERT | DELETE | REFRESH | CLEAN_INSERT", required = true)
    private String operation;

    public ImportOption() {
        this(Parameter.none());
    }

    public ImportOption(Parameter param) {
        super(param);
    }

    @Override
    protected void assertDirectoryExists(CmdLineParser parser) throws CmdLineException {
        this.assertFileParameter(parser, this.srcType, this.src, "src");
    }

    public ImportDataSet targetDataSet() throws DataSetException {
        return new ImportDataSet(this.getComparableDataSetLoader().loadDataSet(
                this.getDataSetParamBuilder()
                        .setSrc(this.src)
                        .setSource(DataSourceType.fromString(this.srcType))
                        .build()
        ));
    }

    public DatabaseOperation operation() {
        return Operation.valueOf(this.operation).op;
    }

    enum Operation {
        INSERT(DatabaseOperation.INSERT),
        UPDATE(DatabaseOperation.UPDATE),
        DELETE(DatabaseOperation.DELETE),
        REFRESH(DatabaseOperation.REFRESH),
        CLEAN_INSERT(DatabaseOperation.CLEAN_INSERT),;
        private final DatabaseOperation op;

        Operation(DatabaseOperation op) {
            this.op = new CloseConnectionOperation(op);
        }
    }
}
