package yo.dbunitcli.dataset;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import yo.dbunitcli.application.DataSourceType;
import yo.dbunitcli.compare.ColumnSetting;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ComparableDataSetLoader {

    private final String url;
    private final String user;
    private final String pass;

    public ComparableDataSetLoader() {
        this(null, null, null);
    }

    public ComparableDataSetLoader(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public ComparableDataSet loadDataSet(File aDir, String aEncoding, DataSourceType aSource, ColumnSetting excludeColumns) throws DataSetException {
        switch (aSource) {
            case TABLE:
                return new ComparableDBDataSet(this.createIDatabaseConnection(), aDir, aEncoding, excludeColumns);
            case SQL:
                return new ComparableQueryDataSet(this.createIDatabaseConnection(), aDir, aEncoding, excludeColumns);
            case XLSX:
                return new ComparableXlsxDataSet(aDir, excludeColumns);
            case XLS:
                return new ComparableXlsDataSet(aDir, excludeColumns);
            case CSVQ:
                return new ComparableCSVQueryDataSet(aDir, aEncoding, excludeColumns);
            case CSV:
                return new ComparableCSVDataSet(aDir, aEncoding, excludeColumns);
        }
        return null;
    }

    public ComparableDataSet loadDataSet(File file, String encoding) throws DataSetException {
        final String fileName = file.getName();
        if (fileName.endsWith(".xlsx")) {
            return new ComparableXlsxDataSet(file);
        } else if (fileName.endsWith(".xls")) {
            return new ComparableXlsDataSet(file);
        }
        return new ComparableCSVDataSet(file, encoding);
    }

    private IDatabaseConnection createIDatabaseConnection() throws DataSetException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection(this.url, this.user, this.pass);
            IDatabaseConnection result = new DatabaseConnection(conn, user);
            DatabaseConfig config = result.getConfig();
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, Boolean.TRUE);
            config.setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, Boolean.TRUE);
            return result;
        } catch (ClassNotFoundException | SQLException | DatabaseUnitException ex) {
            throw new DataSetException(ex);
        }
    }

}
