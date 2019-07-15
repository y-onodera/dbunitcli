package yo.dbunitcli.application;

import org.dbunit.DatabaseUnitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

public class Import implements Command<ImportOption> {

    private static final Logger logger = LoggerFactory.getLogger(Import.class);

    public static void main(String[] args) throws Exception {
        new Import().exec(args);
    }

    @Override
    public ImportOption getOptions() {
        return new ImportOption();
    }

    @Override
    public ImportOption getOptions(Map<String, Object> param) {
        return new ImportOption(param);
    }

    @Override
    public void exec(ImportOption options) throws DatabaseUnitException, SQLException {
        options.operation().execute(options.createIDatabaseConnection(), options.targetDataSet());
    }

}
