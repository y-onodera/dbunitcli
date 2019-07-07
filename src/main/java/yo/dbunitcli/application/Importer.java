package yo.dbunitcli.application;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.IDataSetWriter;

import java.io.File;
import java.sql.SQLException;

public class Importer {
    private static final Logger logger = LoggerFactory.getLogger(Importer.class);

    public static void main(String[] args) throws DatabaseUnitException, SQLException {
        ImportOption options = new ImportOption();
        try {
            options.parse(args);
        } catch (Exception exp) {
            logger.error("option parse failed.", exp);
            System.exit(2);
        }
        options.operation().execute(options.createIDatabaseConnection(), options.targetDataSet());
    }
}
