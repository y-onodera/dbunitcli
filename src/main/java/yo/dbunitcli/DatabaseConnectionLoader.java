package yo.dbunitcli;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionLoader {
    private final Properties jdbcProp;

    public DatabaseConnectionLoader(Properties jdbcProp) {
        this.jdbcProp = jdbcProp;
    }

    public IDatabaseConnection loadConnection() throws DataSetException {
        String url = this.jdbcProp.get("url").toString();
        String user = this.jdbcProp.get("user").toString();
        String pass = this.jdbcProp.get("pass").toString();
        IDatabaseConnection result;
        try {
            if (url.contains("jdbc:oracle:thin")) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                result = new DatabaseConnection(conn, user);
                DatabaseConfig config = result.getConfig();
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
                config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, Boolean.TRUE);
                config.setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, Boolean.TRUE);
                config.setProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, Boolean.TRUE);
            } else if (url.contains("jdbc:sqlserver")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                result = new DatabaseConnection(conn, conn.getCatalog());
                DatabaseConfig config = result.getConfig();
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
                config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, Boolean.TRUE);
                config.setProperty(DatabaseConfig.FEATURE_BATCHED_STATEMENTS, Boolean.TRUE);
            } else {
                throw new UnsupportedOperationException("unknown url :" + url);
            }
            return result;
        } catch (ClassNotFoundException | SQLException | DatabaseUnitException ex) {
            throw new DataSetException(ex);
        }
    }
}
